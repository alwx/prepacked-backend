(ns co.prepacked.city.core
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [honey.sql :as sql]))

(defn all-cities []
  (let [query {:select [:name]
               :from   [:city]}
        result (jdbc/query (database/db) (sql/format query))
        res {:cities (mapv :name result)}]
    [true res]))