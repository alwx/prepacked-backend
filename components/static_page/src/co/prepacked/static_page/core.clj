(ns co.prepacked.static-page.core
  (:require [java-time]
            [clojure.java.jdbc :as jdbc]
            [co.prepacked.city.interface-ns :as city]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.static-page.store :as store]))

(defn static-pages [city-id]
  (store/static-pages city-id))

(defn add-static-page! [city-slug {:keys [slug] :as static-page-data}]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [_ (store/find-by-slug con city-id slug)]
        [false {:errors {:slug ["A static page with the provided slug already exists."]} :-code 400}]
        (let [now (java-time/instant)
              static-page-data' (merge static-page-data
                                       {:city_id city-id
                                        :created_at now
                                        :updated_at now})]
          (store/insert-static-page! con static-page-data')
          (if-let [static-page (store/find-by-slug con city-id slug)]
            [true static-page]
            [false {:errors {:other ["Cannot insert the static page into the database."]} :-code 500}])))
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn update-static-page! [city-slug static-page-slug static-page-data]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-static-page-id :id} (store/find-by-slug con city-id static-page-slug)]
        (let [now (java-time/instant)
              static-page-data' (merge static-page-data
                                       {:city_id city-id
                                        :updated_at now})]
          (store/update-static-page! con city-static-page-id static-page-data')
          (if-let [static-page (store/find-by-slug con city-id (:slug static-page-data))]
            [true static-page]
            [false {:errors {:other ["Cannot update the static page in the database."]} :-code 500}]))
        [false {:errors {:static_page ["A static page with the provided slug doesn't exist."]} :-code 404}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))

(defn delete-static-page! [city-slug static-page-slug]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{city-id :id} (city/city-by-slug city-slug)]
      (if-let [{city-static-page-id :id} (store/find-by-slug con city-id static-page-slug)]
        (do
          (store/delete-static-page! con city-static-page-id)
          [true nil])
        [false {:errors {:other ["Cannot find the static page in the database."]} :-code 500}])
      [false {:errors {:city ["There is no city with the specified slug."]} :-code 404}])))
