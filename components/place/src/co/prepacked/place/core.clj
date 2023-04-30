(ns co.prepacked.place.core
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [co.prepacked.feature.interface-ns :as feature]
    [co.prepacked.file.interface-ns :as file]
    [co.prepacked.place.osm :as osm]
    [co.prepacked.place.store :as store]))

(defn all-places []
  (store/all-places))

(defn place-with-all-dependencies [place-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [place (store/find-by-id con place-id)]
      (let [features (store/place-features con place-id)
            files (store/place-files con place-id)
            places-lists (store/places-lists con place-id)]
        [true (assoc place
                :features features
                :files files
                :places_lists places-lists)])
      [false {:errors {:place ["Place not found."]} :-code 404}])))

(defn place-with-all-dependencies-by-slug [slug]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [place (store/find-by-slug con slug)]
      (let [features (store/place-features con (:id place))
            files (store/place-files con (:id place))
            places-lists (store/places-lists con (:id place))]
        [true (assoc place
                :features features
                :files files
                :places_lists places-lists)])
      [false {:errors {:place ["Place not found."]} :-code 404}])))

(defn places-for-places-list-with-all-dependencies [con city-id places-list-id]
  (let [places (store/places con city-id places-list-id)
        features (->> 
                   (store/places-list-features con places-list-id)
                   (group-by :place_id))
        files (->> 
                (store/places-list-files con places-list-id)
                (group-by :place_id))
        places' (->> 
                  places
                  (mapv (fn [{:keys [id] :as place}]
                          (assoc place
                            :features (get features id [])
                            :files (get files id [])))))]
    [true places']))

(defn place-by-id [con id]
  (store/find-by-id con id))

(defn fetch-osm-data [address]
  (osm/request-place-osm-data address))

(defn add-place! [auth-user place-data]
  (jdbc/with-db-transaction [con (database/db)]
    (let [place-data' (merge place-data {:user_id (:id auth-user)})
          place-id (store/insert-place! con place-data')]
      (if-let [place (store/find-by-id con place-id)]
        [true place]
        [false {:errors {:other ["Cannot insert the place into the database."]} :-code 500}]))))

(defn update-place! [place-id place-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [old-place-data (store/find-by-id con place-id)]
      (let [place-data' (merge old-place-data place-data)]
        (store/update-place! con place-id place-data')
        (if-let [place (store/find-by-id con place-id)]
          [true place]
          [false {:errors {:other ["Cannot update the place in the database."]} :-code 500}]))
      [false {:errors {:place ["A place with the provided id doesn't exist."]} :-code 404}])))

(defn delete-place! [place-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con place-id)
      (do
        (store/delete-place! con place-id)
        [true nil])
      [false {:errors {:other ["Cannot find the place in the database."]} :-code 404}])))

;; operations with `place-features`

(defn add-feature-to-place! [place-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con place-id)
      (if (feature/feature-by-id con (:feature_id input))
        (let [input' (merge input {:place_id place-id})
              id (store/insert-place-feature! con input')]
          (if-let [place-feature (store/find-place-feature con id)]
            [true place-feature]
            [false {:errors {:other ["Cannot add the feature to the place in the database."]} :-code 500}]))
        [false {:errors {:feature ["Feature with the specified id doesn't exist."]} :-code 404}])
      [false {:errors {:place ["There is no place with the specified id."]} :-code 404}])))

(defn update-feature-in-place! [place-id id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con place-id)
      (if (feature/feature-by-id con (:feature_id input))
        (if-let [place-feature (store/find-place-feature con id)]
          (let [input' (merge place-feature input {:place_id place-id})]
            (store/update-place-feature! con id input')
            (if-let [place-feature (store/find-place-feature con id)]
              [true place-feature]
              [false {:errors {:other ["Cannot update the place's feature in the database."]} :-code 500}]))
          [false {:errors {:other ["Cannot find the specified feature for the place."]} :-code 404}])
        [false {:errors {:feature ["There is no feature with the specified id."]} :-code 404}])
      [false {:errors {:place ["There is no place with the specified id."]} :-code 404}])))

(defn delete-feature-in-place! [place-id id]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con place-id)
      (if (store/find-place-feature con id)
        (do
          (store/delete-place-feature! con id)
          [true nil])
        [false {:errors {:other ["Cannot find the specified feature for the place."]} :-code 404}])
      [false {:errors {:city ["There is no place with the specified id."]} :-code 404}])))

;; operations with `place-files`

(defn- add-file-to-place! [con place-id input]
  (if (file/file-by-id con (:file_id input))
    (let [input' (merge input {:place_id place-id})]
      (if (store/find-place-file con place-id (:file-id input'))
        [false {:errors {:place_file ["This file is already added to the specified place."]} :-code 400}]
        (do
          (store/insert-place-file! con input')
          (if-let [place-file (store/find-place-file con place-id (:file_id input'))]
            [true place-file]
            [false {:errors {:other ["Cannot update the place's file in the database."]} :-code 500}]))))
    [false {:errors {:file ["There is no file with the specified id."]} :-code 404}]))

(defn handle-file-upload! [auth-user place-id input]
  (let [{:keys [copyright priority file]} input
        {:keys [content-type]} file]
    (if (file/content-type->supported-ext content-type)
      (jdbc/with-db-transaction [con (database/db)]
        (if (store/find-by-id con place-id)
          (let [[ok? res-file] (file/handle-file-upload! con auth-user file {:copyright copyright})]
            (if ok?
              (let [[ok? res] (add-file-to-place! con place-id {:file_id (:id res-file) 
                                                                :priority (int priority)})]
                (if ok?
                  [true {:file res-file :place_file res}]
                  [false res]))
              [false res-file]))
          [false {:errors {:place ["The place with the specified ID doesn't exist."]} :-code 404}]))
      [false {:errors {:file ["Invalid file."]} :-code 422}])))

(defn update-file-in-place! [place-id file-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [place-file (store/find-place-file con place-id file-id)]
      (let [input' (merge place-file input)]
        (store/update-place-file! con place-id file-id input')
        (if-let [place-file (store/find-place-file con place-id file-id)]
          [true place-file]
          [false {:errors {:other ["Cannot update the place's file in the database."]} :-code 500}]))
      [false {:errors {:other ["Cannot find the specified file for the place."]} :-code 404}])))

(defn delete-file-in-place! [place-id file-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if (store/find-by-id con place-id)
      (if (store/find-place-file con place-id file-id)
        (do
          (store/delete-place-file! con place-id file-id)
          [true nil])
        [false {:errors {:other ["Cannot find the specified file for the place."]} :-code 404}])
      [false {:errors {:city ["There is no place with the specified id."]} :-code 404}])))
