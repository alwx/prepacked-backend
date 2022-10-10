(ns co.prepacked.city.interface-ns
  (:require 
    [co.prepacked.city.core :as core]))

(defn all-cities []
  (core/all-cities))

(defn city-by-slug [slug]
  (core/city-by-slug slug))
