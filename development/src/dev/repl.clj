(ns dev.repl 
  (:require 
    [co.prepacked.rest-api.main :as rest-api]
    [nrepl.server :as nrepl]))

(defn start-repl-server! [] 
  (nrepl/start-server :port 7888))

(defn start-rest-api! []
  (rest-api/start! 
    (Integer/valueOf
      (or (System/getenv "port")
        "6003")
      10)))

(defn stop-rest-api! [] 
  (rest-api/stop!))

(comment
  (start-repl-server!)

  (start-rest-api!)
  (stop-rest-api!)
  
  )
