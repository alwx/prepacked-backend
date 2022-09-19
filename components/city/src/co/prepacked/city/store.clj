(ns co.prepacked.city.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn all-cities []
  (let [query {:select [:*]
               :from   [:city]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [key value]
  (let [query {:select [:*]
               :from   [:city]
               :where  [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-slug [slug]
  (find-by :slug slug))
