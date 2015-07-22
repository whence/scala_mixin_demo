package func_demo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Card

trait Connection {
  def send(content: String) = ???
  def receive(): String = ???
}

object PlayerModule {
  case class Player(name: String, hand: Vector[Card])

  def local_ask(player: Player, question: String, items: Vector[String]): String = {
    println(question)
    items.foreach(println)
    val result = io.StdIn.readLine()
    items.find(_ == result).get
  }

  def network_ask(connection: Connection)
                 (player: Player, question: String, items: Vector[String]): String = {
    connection.send(s"$question\n${items.mkString("\n")}")
    val result = connection.receive()
    items.find(_ == result).get
  }

  def ai_ask(allLogs: () => Seq[String])
            (player: Player, question: String, items: Vector[String]): String = {
    def magic(logs: Seq[String], hand: Vector[Card]): String = ???

    magic(allLogs(), player.hand)
  }

  def audit_ask(log: String => Unit)
               (ask: (Player, String, Vector[String]) => String)
               (player: Player, question: String, items: Vector[String]): String = {
    val result = ask(player, question, items)
    log(result)
    result
  }

  def global_log(logStore: mutable.Buffer[String])
                (thing: String) = {
    logStore.append(thing)
  }

  def global_allLogs(logStore: mutable.Buffer[String]): Seq[String] = {
    logStore.toSeq
  }
}

object SampleApp {
  val gameLog = new ArrayBuffer[String]

  type Ask = (PlayerModule.Player, String, Vector[String]) => String

  val localAsk: Ask = {
    import PlayerModule._
    val ask: Ask = local_ask
    val log = global_log(gameLog)_
    audit_ask(log)(ask)
  }

  val networkAsk: Ask = {
    import PlayerModule._
    val ask: Ask = network_ask(new Connection {})
    val log = global_log(gameLog)_
    audit_ask(log)(ask)
  }

  val aiAsk: Ask = {
    import PlayerModule._
    val ask: Ask = ai_ask(() => global_allLogs(gameLog))
    val log = global_log(gameLog)_
    audit_ask(log)(ask)
  }

  val asks = Vector(localAsk, networkAsk, aiAsk)
}
