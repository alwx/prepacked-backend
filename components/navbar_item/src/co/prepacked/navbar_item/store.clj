(ns co.prepacked.navbar-item.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn navbar-items [city-id]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:= :city_id city-id]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:and
                        [:= key value]
                        [:= :city_id city-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con city-id id]
  (find-by con city-id :id id))

(defn insert-navbar-item! [con navbar-item-input]
  (let [result (jdbc/insert! con :navbar_item navbar-item-input {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-navbar-item! [con id navbar-item-input]
  (let [query {:update :navbar_item
               :set    navbar-item-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-navbar-item! [con id]
  (let [query {:delete-from :navbar_item
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
