(ns co.prepacked.navbar-item.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn get-navbar-items [city-id]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:= :city_id city-id]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [city-id key value]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:and
                        [:= key value]
                        [:= :city_id city-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [city-id id]
  (find-by city-id :id id))

(defn insert-navbar-item! [navbar-item-input]
  (let [result (jdbc/insert!
                (database/db)
                :navbar_item
                navbar-item-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-navbar-item! [id navbar-item-input]
  (let [query {:update :navbar_item
               :set    navbar-item-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-navbar-item! [id]
  (let [query {:delete-from :navbar_item
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
