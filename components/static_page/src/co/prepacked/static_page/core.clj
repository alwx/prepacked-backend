(ns co.prepacked.static-page.core
  (:require 
    [co.prepacked.static-page.store :as store]
    [co.prepacked.city.store :as city.store]))

(defn city-static-pages [city-id]
  (let [static-pages (store/all-city-static-pages city-id)]
    [true static-pages]))

(defn add-static-page! [city-slug {:keys [slug] :as static-page-data}]
  (if-let [city (city.store/find-by-slug city-slug)]
    (if-let [_ (store/find-by-slug (:id city) slug)]
      [false {:errors {:slug ["A static page with the provided slug already exists."]}}]
      (do
        (store/insert-static-page! (assoc static-page-data :city_id (:id city)))
        (if-let [static-page (store/find-by-slug (:id city) slug)]
          [true static-page]
          [false {:errors {:other ["Cannot insert the static page into the database."]}}])))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-static-page! [city-slug static-page-slug static-page-data]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-static-page-id :id} (store/find-by-slug city-id static-page-slug)]
      (do 
        (store/update-static-page! city-static-page-id static-page-data)
        (if-let [static-page (store/find-by-slug city-id (:slug static-page-data))]
          [true static-page]
          [false {:errors {:other ["Cannot update the static page in the database."]}}]))
      [false {:errors {:slug ["A static page with the provided slug doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-static-page! [city-slug static-page-slug]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-static-page-id :id} (store/find-by-slug city-id static-page-slug)]
      (do 
        (store/delete-static-page! city-static-page-id)
        [true nil])
      [false {:errors {:other ["Cannot find the static page in the database."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
