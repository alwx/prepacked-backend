(ns co.prepacked.city.interface-ns
  (:require 
    [co.prepacked.city.core :as core]))

(defn cities []
  (core/cities))

(defn city-with-all-dependencies [slug]
  (core/city-with-all-dependencies slug))