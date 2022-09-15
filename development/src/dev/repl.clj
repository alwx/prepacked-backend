(ns dev.repl 
  (:require [nrepl.server :as nrepl]))

(defonce server 
  (nrepl/start-server :port 7888))
