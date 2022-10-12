(ns co.prepacked.navbar-item.core
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.navbar-item.store :as store]
            [co.prepacked.city.interface-ns :as city]))

(defn navbar-items [city-id]
  (store/navbar-items city-id))

(defn add-navbar-item! [city-slug navbar-item-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (let [navbar-item-data' (assoc navbar-item-data :city_id city-id)
            navbar-item-id (store/insert-navbar-item! con navbar-item-data')]
        (if-let [navbar-item (store/find-by-id con city-id navbar-item-id)]
          [true navbar-item]
          [false {:errors {:other ["Cannot insert the navigation bar item into the database."]} :-code 500}]))
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-navbar-item! [city-slug navbar-item-id navbar-item-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if (store/find-by-id con city-id navbar-item-id)
        (do
          (store/update-navbar-item! con navbar-item-id navbar-item-data)
          (if-let [navbar-item (store/find-by-id con city-id navbar-item-id)]
            [true navbar-item]
            [false {:errors {:other ["Cannot update the navigation bar item in the database."]} :-code 500}]))
        [false {:errors {:navbar_item ["A navigation bar item with the provided id doesn't exist."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn delete-navbar-item! [city-slug navbar-item-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if (store/find-by-id con city-id navbar-item-id)
        (do
          (store/delete-navbar-item! con navbar-item-id)
          [true nil])
        [false {:errors {:other ["Cannot find the navigation item in the database."]} :-code 500}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))
