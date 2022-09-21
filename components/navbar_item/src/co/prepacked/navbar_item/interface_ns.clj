(ns co.prepacked.navbar-item.interface-ns
  (:require 
    [co.prepacked.navbar-item.core :as core]))

(defn city-navbar-items [city-id]
  (core/city-navbar-items city-id))

(defn add-navbar-item! [city-slug navbar-item-input]
  (core/add-navbar-item! city-slug navbar-item-input))
