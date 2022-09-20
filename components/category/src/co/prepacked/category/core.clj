(ns co.prepacked.category.core
  (:require 
    [co.prepacked.category.store :as store]
    [co.prepacked.city.store :as city.store]))

(defn city-categories [city-id]
  (let [categories (store/all-city-categories city-id)]
    [true categories]))

(defn add-category! [city-slug {:keys [slug] :as category-data}]
  (if-let [city (city.store/find-by-slug city-slug)]
    (if-let [_ (store/find-by-slug (:id city) slug)]
      [false {:errors {:slug ["A category with the provided slug already exists."]}}]
      (do
        (store/insert-category! (assoc category-data :city_id (:id city)))
        (if-let [category (store/find-by-slug (:id city) slug)]
          [true category]
          [false {:errors {:other ["Cannot insert category into database."]}}])))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
