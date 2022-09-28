(ns co.prepacked.place.core
  (:require
   [java-time]
   [co.prepacked.city.store :as city.store]
   [co.prepacked.places-list.store :as places-list.store]
   [co.prepacked.place.osm :as osm]
   [co.prepacked.place.store :as store]))

(defn get-places [city-id places-list-id]
  (let [places (store/get-places city-id places-list-id)]
    [true places]))

(defn add-place! [city-slug places-list-slug place-data]
  (if-let [{city-id :id :as city} (city.store/find-by-slug city-slug)]
    (if-let [{places-list-id :id} (places-list.store/find-by-slug city-id places-list-slug)]
      (let [now (java-time/instant)
            osm-data (osm/request-place-osm-data city place-data)
            place-data' (merge place-data
                               osm-data
                               {:city_id city-id
                                :places_list_id places-list-id
                                :created_at now
                                :updated_at now})
            place-id (store/insert-place! place-data')]
        (if-let [place (store/find-by-id city-id places-list-id place-id)]
          [true place]
          [false {:errors {:other ["Cannot insert the place into the database."]}}]))
      [false {:errors {:places-list ["There is no places list with the specified slug."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-place! [city-slug places-list-slug place-id place-data]
  (if-let [{city-id :id :as city} (city.store/find-by-slug city-slug)]
    (if-let [{city-places-list-id :id} (places-list.store/find-by-slug city-id places-list-slug)]
      (if-let [old-place-data (store/find-by-id city-id city-places-list-id place-id)]
        (let [now (java-time/instant)
              osm-data (when (not= (:address old-place-data) (:address place-data))
                         (osm/request-place-osm-data city place-data))
              place-data' (merge old-place-data 
                                 place-data 
                                 osm-data
                                 {:updated_at now})]
          (store/update-place! place-id place-data')
          (if-let [place (store/find-by-id city-id city-places-list-id place-id)]
            [true place]
            [false {:errors {:other ["Cannot update the place in the database."]}}]))
        [false {:errors {:place ["A place with the provided id doesn't exist."]}}])
      [false {:errors {:places_list ["A places list with the provided slug doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-place! [city-slug places-list-slug place-id]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-places-list-id :id} (places-list.store/find-by-slug city-id places-list-slug)]
      (if (store/find-by-id city-id city-places-list-id place-id)
        (do
          (store/delete-place! place-id)
          [true nil])
        [false {:errors {:other ["Cannot find the place in the database."]}}])
      [false {:errors {:places_list ["A places list with the provided slug doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
