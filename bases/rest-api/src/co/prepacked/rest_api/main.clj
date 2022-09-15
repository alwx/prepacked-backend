(ns co.prepacked.rest-api.main
  (:require [co.prepacked.city.interface-ns :as city])
  (:gen-class))

(defn go []
  (println (city/hello "hey!"))
  (System/exit 0))

(defn -main [& args]
  (println (city/hello (first args)))
  (System/exit 0))
