(ns co.prepacked.places-list.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn places-lists [city-id]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:= :city_id city-id]
               :order-by [[:priority :desc]]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [city-id key value]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:and
                       [:= key value]
                       [:= :city_id city-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-slug [city-id slug]
  (find-by city-id :slug slug))

(defn insert-places-list! [places-list-input]
  (jdbc/insert! (database/db) :places_list places-list-input))

(defn update-places-list! [id places-list-input]
  (let [query {:update :places_list
               :set    places-list-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-places-list! [id]
  (let [query {:delete-from :places_list
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn find-places-list-place [places-list-id place-id]
  (let [query {:select [:*]
               :from   [:places_list_place]
               :where  [:and
                        [:= :places_list_id places-list-id]
                        [:= :place-id place-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn insert-places-list-place! [input]
  (jdbc/insert! (database/db) :places_list_place input))

(defn update-places-list-place! [places-list-id place-id places-list-input]
  (let [query {:update :places_list_place
               :set    places-list-input
               :where  [:and
                        [:= :places_list_id places-list-id]
                        [:= :place-id place-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-places-list-place! [places-list-id place-id]
  (let [query {:delete-from :places_list_place
               :where [:and
                       [:= :places_list_id places-list-id]
                       [:= :place-id place-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))
