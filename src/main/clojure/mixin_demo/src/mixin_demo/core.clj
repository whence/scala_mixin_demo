(ns mixin-demo.core
  (:gen-class))

(defmulti ask (fn [player _ _] (:type player)))

(defmethod ask :local
  [player question items]
  (println (:name player) ":" question)
  (doseq [i items] (println i))
  (read-line))

(defmulti send-message (fn [connection _] (:type connection)))

(defmethod send-message :dummy
  [connection message]
  (println "pretend sending" message "to ip" (:ip connection)))

(defmulti receive-message :type)

(defmethod receive-message :dummy
  [connection]
  (println "pretend receiving from ip" (:ip connection))
  :do-it)

(defmethod ask :network
  [player question items]
  (let [connection (:connection player)
        message (clojure.string/join "\n" (into [question] items))]
    (send-message connection message)
    (receive-message connection)))

(def all-logs (atom []))

(defmethod ask :ai
  [player question items]
  (println "making decision from logs" @all-logs "and cards" (:cards player))
  :smart-move)

(def ask
  (let [wrap (fn [ask]
              (fn [player question items]
                (let [choice (ask player question items)
                      entry [(:name player) choice]]
                  (println "logging" entry)
                  (swap! all-logs conj entry)
                  choice)))]
    (wrap ask)))

(defn -main [& args]
  (let [local-player {:type :local :name "local" :cards nil}
        network-player {:type :network :connection {:type :dummy :ip "localhost"} :name "network" :cards nil}
        ai-player {:type :ai :name "ai" :cards [:card3 :card1]}
        players [local-player network-player ai-player ai-player]]
    (doseq [player players]
      (let [answer (ask player "question?" [:item1 :item2 :item3])]
        (println "answer of" (:name player) "is" answer)))))
