(ns co.prepacked.place.core
  (:require [java-time]
            [co.prepacked.place.osm :as osm]
            [co.prepacked.place.store :as store]
            [co.prepacked.feature.interface-ns :as feature]
            [co.prepacked.file.interface-ns :as file]))

(defn places-with-all-dependencies [city-id places-list-id]
  (let [places (store/places city-id places-list-id)
        features (->> (store/places-list-features places-list-id)
                      (group-by :place_id))
        files (->> (store/places-list-files places-list-id)
                   (group-by :place_id))
        places' (->> places
                     (mapv (fn [{:keys [id] :as place}]
                             (assoc place :features (get features id []) :files (get files id [])))))]
    [true places']))

(defn place-by-id [id]
  (store/find-by-id id))

(defn add-place! [auth-user place-data]
  (let [now (java-time/instant)
        osm-data (osm/request-place-osm-data place-data)
        place-data' (merge place-data
                           osm-data
                           {:user_id (:id auth-user)
                            :created_at now
                            :updated_at now})
        place-id (store/insert-place! place-data')]
    (if-let [place (store/find-by-id place-id)]
      [true place]
      [false {:errors {:other ["Cannot insert the place into the database."]}}])))

(defn update-place! [place-id place-data]
  (if-let [old-place-data (store/find-by-id place-id)]
    (let [now (java-time/instant)
          osm-data (when (not= (:address old-place-data) (:address place-data))
                     (osm/request-place-osm-data place-data))
          place-data' (merge old-place-data
                             place-data
                             osm-data
                             {:updated_at now})]
      (store/update-place! place-id place-data')
      (if-let [place (store/find-by-id place-id)]
        [true place]
        [false {:errors {:other ["Cannot update the place in the database."]}}]))
    [false {:errors {:place ["A place with the provided id doesn't exist."]}}])
  [false {:errors {:city ["There is no city with the specified slug."]}}])

(defn delete-place! [place-id]
  (if (store/find-by-id place-id)
    (do
      (store/delete-place! place-id)
      [true nil])
    [false {:errors {:other ["Cannot find the place in the database."]}}]))

(defn add-feature-to-place! [place-id input]
  (if (store/find-by-id place-id)
    (if (feature/feature-by-id (:feature_id input))
      (let [input' (merge input {:place_id place-id})]
        (store/insert-place-feature! input')
        (if-let [place-feature (store/find-place-feature place-id (:feature_id input'))]
          [true place-feature]
          [false {:errors {:other ["Cannot update the place's feature in the database."]}}]))
      [false {:errors {:city ["There is no feature with the specified id."]}}])
    [false {:errors {:city ["There is no place with the specified id."]}}]))

(defn update-feature-in-place! [place-id feature-id input]
  (if (store/find-by-id place-id)
    (if (feature/feature-by-id (:feature_id input))
      (if-let [place-feature (store/find-place-feature place-id feature-id)]
        (let [input' (merge place-feature input {:place_id place-id})]
          (store/update-place-feature! place-id feature-id input')
          (if-let [place-feature (store/find-place-feature place-id (:feature_id input'))]
            [true place-feature]
            [false {:errors {:other ["Cannot update the place's feature in the database."]}}]))
        [false {:errors {:other ["Cannot find the specified feature for the place."]}}])
      [false {:errors {:city ["There is no feature with the specified id."]}}])
    [false {:errors {:city ["There is no place with the specified id."]}}]))

(defn delete-feature-in-place! [place-id feature-id]
  (if (store/find-by-id place-id)
    (if (store/find-place-feature place-id feature-id)
      (do
        (store/delete-place-feature! place-id feature-id)
        [true nil])
      [false {:errors {:other ["Cannot find the specified feature for the place."]}}])
    [false {:errors {:city ["There is no place with the specified id."]}}]))

(defn add-file-to-place! [place-id input]
  (if (store/find-by-id place-id)
    (if (file/file-by-id (:file_id input))
      (let [input' (merge input {:place_id place-id})]
        (store/insert-place-file! input')
        (if-let [place-file (store/find-place-file place-id (:file_id input'))]
          [true place-file]
          [false {:errors {:other ["Cannot update the place's file in the database."]}}]))
      [false {:errors {:city ["There is no file with the specified id."]}}])
    [false {:errors {:city ["There is no place with the specified id."]}}]))

(defn delete-file-in-place! [place-id file-id]
  (if (store/find-by-id place-id)
    (if (store/find-place-file place-id file-id)
      (do
        (store/delete-place-file! place-id file-id)
        [true nil])
      [false {:errors {:other ["Cannot find the specified file for the place."]}}])
    [false {:errors {:city ["There is no place with the specified id."]}}]))
