(ns co.prepacked.city.interface-ns
  (:require 
    [co.prepacked.city.core :as core]
    [co.prepacked.city.store :as store]))

(defn cities []
  (core/cities))

(defn city-by-id [id]
  (store/find-by-id id))

(defn city-with-all-dependencies [slug]
  (core/city-with-all-dependencies slug))