(ns co.prepacked.place.core
  (:require
   [java-time]
   [co.prepacked.place.osm :as osm]
   [co.prepacked.place.store :as store]))

(defn get-places [city-id places-list-id]
  (let [places (store/get-places city-id places-list-id)]
    [true places]))

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
