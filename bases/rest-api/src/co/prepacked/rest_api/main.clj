(ns co.prepacked.rest-api.main
  (:require [co.prepacked.city.interface-ns :as city])
  (:gen-class))

(defn -main [& args]
  (println (city/hello (first args)))
  (System/exit 0))
