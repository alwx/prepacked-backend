(ns co.prepacked.database.schema
  (:require [clojure.java.jdbc :as jdbc]
    [co.prepacked.log.interface-ns :as log]
    [honey.sql :as sql]))

(def user
  (jdbc/create-table-ddl :user
    [[:id :integer :primary :key :autoincrement]
     [:email :text :unique]
     [:username :text :unique]
     [:password :text]]
    {:entities identity}))

(def city
  (jdbc/create-table-ddl :city
    [[:id :integer :primary :key :autoincrement]
     [:slug :text :unique]
     [:name :text]]
    {:entities identity}))

(def category
  (jdbc/create-table-ddl :category
    [[:id :integer :primary :key :autoincrement]
     [:cityId :integer "references city(id)"]
     [:slug :text :unique]
     [:title :text]
     [:description :text]]
    {:entities identity}))

(def static-page
  (jdbc/create-table-ddl :staticPage
    [[:id :integer :primary :key :autoincrement]
     [:cityId :integer "references city(id)"]
     [:slug :text :unique]
     [:title :text]
     [:content :text]]
    {:entities identity}))

(def navbar-item
  (jdbc/create-table-ddl :navbarItem
    [[:id :integer :primary :key :autoincrement]
     [:cityId :integer "references city(id)"]
     [:title :text]
     [:priority :integer]
     [:contentType :text]
     [:contentId :integer]]
    {:entities identity}))

(defn generate-db [db]
  (jdbc/db-do-commands 
    db
    [user city]))

(defn drop-db [db]
  (jdbc/db-do-commands 
    db
    [(jdbc/drop-table-ddl :user)
     (jdbc/drop-table-ddl :city)
     (jdbc/drop-table-ddl :category)
     (jdbc/drop-table-ddl :staticPage)
     (jdbc/drop-table-ddl :navbarItem)]))

(defn table->schema-item [{:keys [tbl_name sql]}]
  [(keyword tbl_name) sql])

(defn valid-schema? [db]
  (let [query {:select [:*]
               :from   [:sqlite_master]
               :where  [:= :type "table"]}
        tables (jdbc/query db (sql/format query) {:identifiers identity})
        current-schema (select-keys (into {} (map table->schema-item tables))
                         [:user :city :category :staticPage :navbarItem])
        valid-schema {:user user 
                      :city city
                      :category category
                      :staticPage static-page
                      :navbarItem navbar-item}]
    (if (= valid-schema current-schema)
      true
      (do
        (log/warn "Current schema is invalid. Please correct it and restart the server.")
        false))))
