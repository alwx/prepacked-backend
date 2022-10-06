(ns co.prepacked.city.interface-ns
  (:require 
    [co.prepacked.city.core :as core]))

(defn all-cities []
  (core/all-cities))

(defn city-with-all-dependencies [slug]
  (core/city-with-all-dependencies slug))
