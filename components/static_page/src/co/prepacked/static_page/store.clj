(ns co.prepacked.static-page.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn static-pages [city-id]
  (let [query {:select [:*]
               :from   [:static_page]
               :where  [:= :city_id city-id]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from   [:static_page]
               :where  [:and 
                        [:= key value]
                        [:= :city_id city-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-slug [con city-id slug]
  (find-by con city-id :slug slug))

(defn insert-static-page! [con static-page-input]
  (jdbc/insert! con :static_page static-page-input))

(defn update-static-page! [con id static-page-input]
  (let [query {:update :static_page
               :set    static-page-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-static-page! [con id]
  (let [query {:delete-from :static_page
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
