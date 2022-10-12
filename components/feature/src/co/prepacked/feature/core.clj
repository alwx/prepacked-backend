(ns co.prepacked.feature.core
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.feature.store :as store]))

(defn feature-by-id [con id]
  (store/find-by-id con id))

(defn add-feature! [feature-data]
  (jdbc/with-db-transaction [con (database/db)]
    (store/insert-feature! con feature-data)
    (if-let [feature (store/find-by-id con (:id feature-data))]
      [true feature]
      [false {:errors {:other ["Cannot insert the feature into the database."]} :-code 500}])))

(defn update-feature! [feature-id feature-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con feature-id)
      (do (store/update-feature! con feature-id feature-data)
          (if-let [place (store/find-by-id con (:id feature-data))]
            [true place]
            [false {:errors {:other ["Cannot update the feature in the database."]} :-code 500}]))
      [false {:errors {:place ["A feature with the provided id doesn't exist."]} :-code 404}])))

(defn delete-feature! [feature-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con feature-id)
      (do
        (store/delete-feature! con feature-id)
        [true nil])
      [false {:errors {:other ["Cannot find the feature in the database."]} :-code 404}])))
