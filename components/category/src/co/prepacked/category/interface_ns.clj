(ns co.prepacked.category.interface-ns
  (:require 
    [co.prepacked.category.core :as core]))

(defn city-categories [city-id]
  (core/city-categories city-id))

(defn add-category! [city-slug category-input]
  (core/add-category! city-slug category-input))
