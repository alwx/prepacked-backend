(ns co.prepacked.city.core
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.city.store :as store]
            [honey.sql :as sql]))

(defn cities []
  (let [cities (store/all-cities)]
    [true cities]))

(defn city-with-all-dependencies [slug]
  (if-let [city (store/find-by-slug slug)]
    [true {:city city}]
    [false {:errors {:slug ["Cannot find the city."]}}]))
