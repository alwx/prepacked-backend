(ns co.prepacked.rest-api.main
  (:require 
    [co.prepacked.env.interface-ns :as env]
    [co.prepacked.log.interface-ns :as log]
    [co.prepacked.rest-api.api :as api]
    [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(def ^:private server-ref (atom nil))

(defn start!
  [port]
  (if-let [_server @server-ref]
    (log/warn "Server already running? (stop!) it first.")
    (do
      (log/info "Starting server on port: " port)
      (api/init)
      (reset! server-ref
        (run-jetty api/app
          {:port port
           :join? false})))))

(defn stop! []
  (if-let [server @server-ref]
    (do (api/destroy)
      (.stop server)
      (reset! server-ref nil))
    (log/warn "No server")))

(defn -main [& _args]
  (start! 
   (or (env/get-var :port)
       (do
         (log/warn "`:port` needs to be added to `env.edn`!")
         (System/exit 1)))))
