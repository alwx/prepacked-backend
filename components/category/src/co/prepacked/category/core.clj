(ns co.prepacked.category.core
  (:require 
    [co.prepacked.category.store :as store]))

(defn city-categories []
  (let [cities (store/all-cities)]
    [true cities]))