(ns dev.repl 
  (:require 
    [nrepl.server :as nrepl]
    [co.prepacked.rest-api.main :as rest-api]))

(defonce repl-server 
  (nrepl/start-server :port 7888))

(defn start-rest-api! []
  (rest-api/start! 
    (Integer/valueOf
      (or (System/getenv "port")
        "6003")
      10)))

(defn stop-rest-api! []
  (rest-api/stop!))