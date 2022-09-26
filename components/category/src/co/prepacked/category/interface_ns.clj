(ns co.prepacked.category.interface-ns
  (:require
    [co.prepacked.category.core :as core]))

(defn city-categories [city-id]
  (core/city-categories city-id))

(defn add-category! [city-slug category-input]
  (core/add-category! city-slug category-input))

(defn update-category! [city-slug category-slug category-input]
  (core/update-category! city-slug category-slug category-input))

(defn delete-category! [city-slug category-slug]
  (core/delete-category! city-slug category-slug))