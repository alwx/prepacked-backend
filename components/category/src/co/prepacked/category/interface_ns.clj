(ns co.prepacked.category.interface-ns
  (:require 
    [co.prepacked.category.core :as core]))

(defn city-categories [city-slug]
  (core/city-categories city-slug))
