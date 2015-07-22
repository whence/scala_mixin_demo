package mixin_demo

trait Card

trait Connection {
  def send(content: String)
  def receive(): String
}

class Player(val name: String) {
  def hand: Vector[Card] = Vector.empty
}

trait Interactive {
  def choose(question: String, items: Vector[String]): String
}

trait LocalInteractive extends Interactive {
  def choose(question: String, items: Vector[String]): String = {
    println(question)
    items.foreach(println)
    io.StdIn.readLine()
  }
}

trait NetworkInteractive extends Interactive {
  def connection: Connection

  def choose(question: String, items: Vector[String]): String = {
    connection.send(s"$question\n${items.mkString("\n")}")
    connection.receive()
  }
}

trait AIInteractive extends Interactive {
  def hand: Vector[Card]
  def allLogs: Seq[String]

  def choose(question: String, items: Vector[String]): String = ???
}

trait AuditingInteractive extends Interactive {
  def log(choice: String)

  abstract override def choose(question: String, items: Vector[String]): String = {
    val choice = super.choose(question, items)
    log(choice)
    choice
  }
}

trait LoggingRepository {
  def log(choice: String) = ???
  def allLogs: Seq[String] = ???
}

object SampleApp {
  val dummyConnection = new Connection {
    def send(content: String): Unit = ???
    def receive(): String = ???
  }

  val localPlayer = new Player("local") with LocalInteractive with AuditingInteractive with LoggingRepository

  val networkPlayer = new Player("network") with NetworkInteractive with AuditingInteractive with LoggingRepository {
    val connection = dummyConnection
  }

  val aiPlayer = new Player("ai") with AIInteractive with AuditingInteractive with LoggingRepository

  val players = Vector(localPlayer, networkPlayer, aiPlayer)
}
