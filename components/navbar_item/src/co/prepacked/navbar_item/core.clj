(ns co.prepacked.navbar-item.core
  (:require 
    [co.prepacked.navbar-item.store :as store]
    [co.prepacked.log.interface-ns :as log]
    [co.prepacked.city.store :as city.store]))

(defn city-navbar-items [city-id]
  (let [navbar-items (store/all-city-navbar-items city-id)]
    [true navbar-items]))

(defn add-navbar-item! [city-slug {:keys [id] :as navbar-item-data}]
  (if-let [city (city.store/find-by-slug city-slug)]
    (let [navbar-item-id (-> 
                           (assoc navbar-item-data :city_id (:id city))
                           (store/insert-navbar-item!)
                           (first)
                           (vals)
                           (first))]
      (if-let [navbar-item (store/find-by-id (:id city) navbar-item-id)]
        [true navbar-item]
        [false {:errors {:other ["Cannot insert navbar item into database."]}}]))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
