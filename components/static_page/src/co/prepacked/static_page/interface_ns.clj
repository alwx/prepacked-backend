(ns co.prepacked.static-page.interface-ns
  (:require 
    [co.prepacked.static-page.core :as core]))

(defn city-static-pages [city-id]
  (core/city-static-pages city-id))

(defn add-static-page! [city-slug static-page-input]
  (core/add-static-page! city-slug static-page-input))

(defn update-static-page! [city-slug static-page-slug static-page-input]
  (core/update-static-page! city-slug static-page-slug static-page-input))

(defn delete-static-page! [city-slug static-page-slug]
  (core/delete-static-page! city-slug static-page-slug))
