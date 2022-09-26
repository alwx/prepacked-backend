(ns co.prepacked.category.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn all-city-categories [city-id]
  (let [query {:select [:*]
               :from   [:category]
               :where  [:= :city_id city-id]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [city-id key value]
  (let [query {:select [:*]
               :from   [:category]
               :where  [:and 
                        [:= key value]
                        [:= :city_id city-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-slug [city-id slug]
  (find-by city-id :slug slug))

(defn insert-category! [category-input]
  (jdbc/insert! (database/db) :category category-input))

(defn update-category! [id category-input]
  (let [query {:update :category
               :set    category-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-category! [id]
  (let [query {:delete-from :category
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
