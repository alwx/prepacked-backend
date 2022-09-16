(ns co.prepacked.database.schema
  (:require [clojure.java.jdbc :as jdbc]
            [co.prepacked.log.interface-ns :as log]
            [honey.sql :as sql]))

(def city
  (jdbc/create-table-ddl :city
                         [[:id :integer :primary :key :autoincrement]
                          [:slug :text :unique]
                          [:name :text]]
                         {:entities identity}))

(defn generate-db [db]
  (jdbc/db-do-commands db
                       [city]))

(defn drop-db [db]
  (jdbc/db-do-commands db
                       [(jdbc/drop-table-ddl :city)]))

(defn table->schema-item [{:keys [tbl_name sql]}]
  [(keyword tbl_name) sql])

(defn valid-schema? [db]
  (let [query {:select [:*]
               :from   [:sqlite_master]
               :where  [:= :type "table"]}
        tables (jdbc/query db (sql/format query) {:identifiers identity})
        current-schema (select-keys (into {} (map table->schema-item tables))
                                    [:city])
        valid-schema {:city city}]
    (if (= valid-schema current-schema)
      true
      (do
        (log/warn "Current schema is invalid. Please correct it and restart the server.")
        false))))
