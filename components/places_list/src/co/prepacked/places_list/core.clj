(ns co.prepacked.places-list.core
  (:require
   [java-time]
   [co.prepacked.places-list.store :as store]
   [co.prepacked.city.store :as city.store]))

(defn city-places-lists [city-id]
  (let [places-lists (store/all-places-lists city-id)]
    [true places-lists]))

(defn add-places-list! [city-slug {:keys [slug] :as places-list-data}]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [_ (store/find-by-slug city-id slug)]
      [false {:errors {:slug ["A places list with the provided slug already exists."]}}]
      (let [now (java-time/instant)
            places-list-data' (merge places-list-data
                                     {:city_id city-id
                                      :created_at now
                                      :updated_at now})]
        (store/insert-places-list! places-list-data')
        (if-let [places-list (store/find-by-slug city-id slug)]
          [true places-list]
          [false {:errors {:other ["Cannot insert the places list into the database."]}}])))
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn update-places-list! [city-slug places-list-slug places-list-data]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-places-list-id :id} (store/find-by-slug city-id places-list-slug)]
      (let [now (java-time/instant)
            places-list-data' (merge places-list-data 
                                     {:updated_at now})]
        (store/update-places-list! city-places-list-id places-list-data')
        (if-let [places-list (store/find-by-slug city-id (:slug places-list-data'))]
          [true places-list]
          [false {:errors {:other ["Cannot update the places list in the database."]}}]))
      [false {:errors {:slug ["A places list with the provided slug doesn't exist."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))

(defn delete-places-list! [city-slug places-list-slug]
  (if-let [{city-id :id} (city.store/find-by-slug city-slug)]
    (if-let [{city-places-list-id :id} (store/find-by-slug city-id places-list-slug)]
      (do
        (store/delete-places-list! city-places-list-id)
        [true nil])
      [false {:errors {:other ["Cannot find the places list in the database."]}}])
    [false {:errors {:city ["There is no city with the specified slug."]}}]))
