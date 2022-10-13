(ns co.prepacked.places-list.core
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.city.interface-ns :as city]
            [co.prepacked.file.interface-ns :as file]
            [co.prepacked.place.interface-ns :as place]
            [co.prepacked.places-list.store :as store]))

(defn places-lists-with-all-dependencies [city-id]
  (jdbc/with-db-transaction [con (database/db)]
    (let [places-lists (store/places-lists con city-id)
          files (->> (store/city-files con city-id)
                     (group-by :places_list_id))
          places-lists' (->> places-lists
                             (mapv (fn [{:keys [id] :as places-list}]
                                     (assoc places-list :files (get files id [])))))]
      places-lists')))

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
      (if (store/find-by-slug con city-id slug)
        [false {:errors {:slug ["A places list with the provided slug already exists."]} :-code 400}]
        (let [places-list-data' (merge places-list-data {:city_id city-id
                                                         :user_id (:id auth-user)})]
          (store/insert-places-list! con places-list-data')
          (if-let [places-list (store/find-by-slug con city-id slug)]
            [true places-list]
            [false {:errors {:other ["Cannot insert the places list into the database."]} :-code 500}])))
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-places-list! [city-slug places-list-slug places-list-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id :as old-places-list-data} (store/find-by-slug con city-id places-list-slug)]
        (let [places-list-data' (merge old-places-list-data
                                       places-list-data)]
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
          (if (store/find-places-list-place con city-places-list-id (:place_id input))
            [false {:errors {:slug ["The place is already added to the specified places list."]} :-code 400}]
            (let [input' (merge input {:places_list_id city-places-list-id
                                       :user_id (:id auth-user)})]
              (store/insert-places-list-place! con input')
              (if-let [places-list-place (store/find-places-list-place con city-places-list-id (:place_id input'))]
                [true places-list-place]
                [false {:errors {:other ["Cannot add the place to the list in the database."]} :-code 500}])))
          [false {:errors {:place ["Place with the specified id doesn't exist."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-place-in-places-list! [city-slug places-list-slug place-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if-let [places-list-place (store/find-places-list-place con city-places-list-id place-id)]
          (let [input' (merge places-list-place input)]
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

(defn- add-file-to-places-list! [con places-list-id input]
  (if (file/file-by-id con (:file_id input))
    (let [input' (merge input {:places_list_id places-list-id})]
      (if (store/find-places-list-file con places-list-id (:file-id input'))
        [false {:errors {:places_list_file ["This file is already added to the specified list of places."]} :-code 400}]
        (do
          (store/insert-places-list-file! con input')
          (if-let [places-list-file (store/find-places-list-file con places-list-id (:file_id input'))]
            [true places-list-file]
            [false {:errors {:other ["Cannot update the place's file in the database."]} :-code 500}]))))
    [false {:errors {:file ["There is no file with the specified id."]} :-code 404}]))

(defn handle-file-upload! [auth-user city-slug places-list-slug input]
  (let [{:keys [copyright priority file]} input
        {:keys [content-type]} file]
    (if (file/content-type->supported-ext content-type)
      (jdbc/with-db-transaction [con (database/db)]
        (if-let [{city-id :id} (city/city-by-slug city-slug)]
          (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
            (let [[ok? res-file] (file/handle-file-upload! con auth-user file {:copyright copyright})]
              (if ok?
                (let [[ok? res] (add-file-to-places-list! con city-places-list-id {:file_id (:id res-file)
                                                                                   :priority (int priority)})]
                  (if ok?
                    [true {:file res-file :places_list_file res}]
                    [false res]))
                [false res-file]))
            [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
          [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}]))
      [false {:errors {:file ["Invalid file."]} :-code 422}])))

(defn update-file-in-places-list! [city-slug places-list-slug file-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if-let [places-list-file (store/find-places-list-file con city-places-list-id file-id)]
          (let [input' (merge places-list-file input)]
            (store/update-places-list-file! con city-places-list-id file-id input')
            (if-let [places-list-file (store/find-places-list-file con city-places-list-id file-id)]
              [true places-list-file]
              [false {:errors {:other ["Cannot update the list's file in the database."]} :-code 500}]))
          [false {:errors {:other ["Cannot find the specified file for the place's list."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn delete-file-in-places-list! [city-slug places-list-slug file-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-places-list-id :id} (store/find-by-slug con city-id places-list-slug)]
        (if (store/find-places-list-file con city-places-list-id file-id)
          (do
            (store/delete-places-list-file! con city-places-list-id file-id)
            [true nil])
          [false {:errors {:other ["Cannot find the specified file for the place's list."]} :-code 404}])
        [false {:errors {:other ["Cannot find the places list in the database."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))