(ns mixin-demo.core
  (:gen-class))

(defn- type1
  "get type of the first argument"
  [x & args] (:type x))

(defmulti choose
  "let the player choose from some items"
  type1)

(defmulti send-message
  "send message to a connection"
  type1)

(defmulti receive-message
  "receive message from a connection"
  type1)

(def all-logs
  "global log store"
  (atom []))

(defn- with-logging
  "wrap a function and log its result and name of the first argument"
  [f]
  (fn [& args]
    (let [result (apply f args)
          name (:name (first args))
          entry [name result]]
      (println "logging" entry)
      (swap! all-logs conj entry)
      result)))

(defmethod choose :local
  [player question items]
  (println (:name player) ":" question)
  (doseq [i items] (println i))
  (read-line))

(defmethod choose :network
  [player question items]
  (let [connection (:connection player)
        message (clojure.string/join "\n" (cons question items))]
    (send-message connection message)
    (receive-message connection)))

(defmethod choose :ai
  [player question items]
  (println "making decision from logs" @all-logs "and hand of" (:hand player))
  :smart-move)

(def choose
  "add logging capability to choose"
  (with-logging choose))

(defmethod send-message :dummy
  [connection message]
  (println "pretend sending" message "to ip" (:ip connection)))

(defmethod receive-message :dummy
  [connection]
  (println "pretend receiving from ip" (:ip connection))
  :do-it)

(defn -main [& args]
  (let [local-player {:type :local :name "local" :hand nil}
        network-player {:type :network :connection {:type :dummy :ip "localhost"} :name "network" :hand nil}
        ai-player {:type :ai :name "ai" :hand [:card3 :card1]}
        players [local-player network-player ai-player ai-player]]
    (doseq [player players]
      (let [answer (choose player "question?" [:item1 :item2 :item3])]
        (println "answer of" (:name player) "is" answer)))))
