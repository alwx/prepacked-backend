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
          [false {:errors {:other ["Cannot insert static page into database."]}}])))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
