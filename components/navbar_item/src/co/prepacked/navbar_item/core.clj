(ns co.prepacked.navbar-item.core
  (:require 
    [co.prepacked.navbar-item.store :as store]
    [co.prepacked.city.store :as city.store]))

(defn city-navbar-items [city-id]
  (let [navbar-items (store/all-city-navbar-items city-id)]
    [true navbar-items]))

(defn add-navbar-item! [city-slug navbar-item-data]
  (if-let [city (city.store/find-by-slug city-slug)]
    (let [navbar-item-id (-> navbar-item-data 
                           (assoc :city_id (:id city))
                           (store/insert-navbar-item!)
                           (first)
                           (vals)
                           (first))]
      (if-let [navbar-item (store/find-by-id (:id city) navbar-item-id)]
        [true navbar-item]
        [false {:errors {:other ["Cannot insert the navigation bar item into the database."]}}]))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-navbar-item! [city-slug navbar-item-id navbar-item-data]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if (store/find-by-id city-id navbar-item-id)
      (do 
        (store/update-navbar-item! navbar-item-id navbar-item-data)
        (if-let [navbar-item (store/find-by-id city-id navbar-item-id)]
          [true navbar-item]
          [false {:errors {:other ["Cannot update the navigation bar item in the database."]}}]))
      [false {:errors {:slug ["A navigation bar item with the provided id doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-navbar-item! [city-slug navbar-item-id]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if (store/find-by-id city-id navbar-item-id)
      (do 
        (store/delete-navbar-item! navbar-item-id)
        [true nil])
      [false {:errors {:other ["Cannot find the navigation item in the database."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
