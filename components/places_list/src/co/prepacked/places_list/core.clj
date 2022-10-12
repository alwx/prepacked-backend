(ns co.prepacked.places-list.core
  (:require [java-time]
            [clojure.java.jdbc :as jdbc] 
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.city.interface-ns :as city]
            [co.prepacked.place.interface-ns :as place]
            [co.prepacked.places-list.store :as store]))

(defn places-lists [city-id]
  (store/places-lists (database/db) city-id))

(defn- add-places-list-dependencies [{:keys [id city_id] :as places-list}]
  (let [[_ places] (place/places-with-all-dependencies city_id id)]
    (assoc places-list :places places)))

(defn places-list-with-all-dependencies [city-slug places-list-slug]
  (if-let [{city-id :id} (city/city-by-slug city-slug)]
    (if-let [places-list (store/find-by-slug (database/db) city-id places-list-slug)]
      [true {:places_list (add-places-list-dependencies places-list)}]
      [false {:errors {:places_list ["Cannot find the places list."]} :-code 404}])
    [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}]))

(defn add-places-list! [auth-user city-slug {:keys [slug] :as places-list-data}]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [_ (store/find-by-slug con city-id slug)]
        [false {:errors {:slug ["A places list with the provided slug already exists."]} :-code 400}]
        (let [now (java-time/instant)
              places-list-data' (merge places-list-data
                                       {:city_id city-id
                                        :user_id (:id auth-user)
                                        :created_at now
                                        :updated_at now})]
          (store/insert-places-list! con places-list-data')
          (if-let [places-list (store/find-by-slug con city-id slug)]
            [true places-list]
            [false {:errors {:other ["Cannot insert the places list into the database."]} :-code 500}])))
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-places-list! [city-slug places-list-slug places-list-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id :as old-places-list-data} (store/find-by-slug con city-id places-list-slug)]
        (let [now (java-time/instant)
              places-list-data' (merge old-places-list-data
                                       places-list-data
                                       {:updated_at now})]
          (store/update-places-list! con city-places-list-id places-list-data')
          (if-let [places-list (store/find-by-slug con city-id (:slug places-list-data'))]
            [true places-list]
            [false {:errors {:other ["Cannot update the places list in the database."]} :-code 500}]))
        [false {:errors {:places_list ["A places list with the provided slug doesn't exist."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn delete-places-list! [city-slug places-list-slug]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (do
          (store/delete-places-list! con city-places-list-id)
          [true nil])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn add-place-to-places-list! [auth-user city-slug places-list-slug input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if (place/place-by-id con (:place_id input))
          (let [now (java-time/instant)
                input' (merge input
                              {:places_list_id city-places-list-id
                               :user_id (:id auth-user)
                               :created_at now
                               :updated_at now})]
          ;; TODO(alwx): check for constraint violation
            (store/insert-places-list-place! con input')
            (if-let [places-list-place (store/find-places-list-place con city-places-list-id (:place_id input'))]
              [true places-list-place]
              [false {:errors {:other ["Cannot add the place to the list in the database."]} :-code 500}]))
          [false {:errors {:place ["Place with the specified id doesn't exist."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-place-in-places-list! [city-slug places-list-slug place-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if-let [places-list-place (store/find-places-list-place con city-places-list-id place-id)]
          (let [now (java-time/instant)
                input' (merge places-list-place
                              input
                              {:updated_at now})]
            (store/update-places-list-place! con city-places-list-id place-id input')
            (if-let [places-list-place' (store/find-places-list-place con city-places-list-id place-id)]
              [true places-list-place']
              [false {:errors {:other ["Cannot update the place in the database."]} :-code 500}]))
          [false {:errors {:other ["Cannot find the specified place in the place list."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn delete-place-in-places-list! [city-slug places-list-slug place-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if (store/find-places-list-place con city-places-list-id place-id)
          (do
            (store/delete-places-list-place! con city-places-list-id place-id)
            [true nil])
          [false {:errors {:other ["Cannot find the specified place in the place list."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))
