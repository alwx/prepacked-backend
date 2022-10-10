(ns co.prepacked.feature.core
  (:require
   [co.prepacked.feature.store :as store]))

(defn feature-by-id [id]
  (store/find-by-id id))

(defn add-feature! [feature-data]
  (store/insert-feature! feature-data)
  (if-let [feature (store/find-by-id (:id feature-data))]
    [true feature]
    [false {:errors {:other ["Cannot insert the feature into the database."]}}]))

(defn update-feature! [feature-id feature-data]
  (if (store/find-by-id feature-id)
    (do (store/update-feature! feature-id feature-data)
        (if-let [place (store/find-by-id (:id feature-data))]
          [true place]
          [false {:errors {:other ["Cannot update the feature in the database."]}}]))
    [false {:errors {:place ["A feature with the provided id doesn't exist."]}}]))

(defn delete-feature! [feature-id]
  (if (store/find-by-id feature-id)
    (do
      (store/delete-feature! feature-id)
      [true nil])
    [false {:errors {:other ["Cannot find the feature in the database."]}}]))
