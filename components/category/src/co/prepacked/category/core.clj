(ns co.prepacked.category.core
  (:require 
    [co.prepacked.category.store :as store]
    [co.prepacked.city.store :as city.store]))

(defn city-categories [city-id]
  (let [categories (store/all-city-categories city-id)]
    [true categories]))

(defn add-category! [city-slug {:keys [slug] :as category-data}]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [_ (store/find-by-slug city-id slug)]
      [false {:errors {:slug ["A category with the provided slug already exists."]}}]
      (do
        (store/insert-category! (assoc category-data :city_id city-id))
        (if-let [category (store/find-by-slug city-id slug)]
          [true category]
          [false {:errors {:other ["Cannot insert the category into the database."]}}])))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-category! [city-slug category-slug category-data]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-category-id :id} (store/find-by-slug city-id category-slug)]
      (do 
        (store/update-category! city-category-id category-data)
        (if-let [category (store/find-by-slug city-id (:slug category-data))]
          [true category]
          [false {:errors {:other ["Cannot update the category in the database."]}}]))
      [false {:errors {:slug ["A category with the provided slug doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-category! [city-slug category-slug]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-category-id :id} (store/find-by-slug city-id category-slug)]
      (do 
        (store/delete-category! city-category-id)
        [true nil])
      [false {:errors {:other ["Cannot find the category in the database."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
