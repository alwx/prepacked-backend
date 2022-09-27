(ns co.prepacked.place.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn get-places [city-id places-list-id]
  (let [query {:select [:*]
               :from   [:place]
               :where  [:and 
                        [:= :city_id city-id]
                        [:= :places_list_id places-list-id]]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [city-id places-list-id key value]
  (let [query {:select [:*]
               :from   [:place]
               :where  [:and
                        [:= key value]
                        [:= :city_id city-id]
                        [:= :places_list_id places-list-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [city-id places-list-id id]
  (find-by city-id places-list-id :id id))

(defn insert-place! [place-input]
  (let [result (jdbc/insert!
                (database/db)
                :place
                place-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-place! [id place-input]
  (let [query {:update :place
               :set    place-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-place! [id]
  (let [query {:delete-from :place
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
