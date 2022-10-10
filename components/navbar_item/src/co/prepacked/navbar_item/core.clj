(ns co.prepacked.navbar-item.core
  (:require
   [co.prepacked.navbar-item.store :as store]
   [co.prepacked.city.interface-ns :as city]))

(defn navbar-items [city-id]
  (store/navbar-items city-id))

(defn add-navbar-item! [city-slug navbar-item-data]
  (if-let [{city-id :id} (city/city-by-slug city-slug)]
    (let [navbar-item-data' (assoc navbar-item-data :city_id city-id)
          navbar-item-id (store/insert-navbar-item! navbar-item-data')]
      (if-let [navbar-item (store/find-by-id city-id navbar-item-id)]
        [true navbar-item]
        [false {:errors {:other ["Cannot insert the navigation bar item into the database."]}}]))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-navbar-item! [city-slug navbar-item-id navbar-item-data]
  (if-let [{city-id :id} (city/city-by-slug city-slug)]
    (if (store/find-by-id city-id navbar-item-id)
      (do
        (store/update-navbar-item! navbar-item-id navbar-item-data)
        (if-let [navbar-item (store/find-by-id city-id navbar-item-id)]
          [true navbar-item]
          [false {:errors {:other ["Cannot update the navigation bar item in the database."]}}]))
      [false {:errors {:navbar_item ["A navigation bar item with the provided id doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-navbar-item! [city-slug navbar-item-id]
  (if-let [{city-id :id} (city/city-by-slug city-slug)]
    (if (store/find-by-id city-id navbar-item-id)
      (do
        (store/delete-navbar-item! navbar-item-id)
        [true nil])
      [false {:errors {:other ["Cannot find the navigation item in the database."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
