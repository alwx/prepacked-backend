(ns co.prepacked.database.schema
  (:require 
    [clojure.java.jdbc :as jdbc]
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
     [:city_id :integer "references city(id)"]
     [:slug :text :unique]
     [:title :text]
     [:description :text]]
    {:entities identity}))

(def static-page
  (jdbc/create-table-ddl :static_page
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:slug :text :unique]
     [:title :text]
     [:content :text]]
    {:entities identity}))

(def navbar-item
  (jdbc/create-table-ddl :navbar_item
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:title :text]
     [:priority :integer]
     [:content_type :text]
     [:content_id :integer]]
    {:entities identity}))

(defn generate-db [db]
  (jdbc/db-do-commands 
    db
    [user city category static-page navbar-item]))

(defn drop-db [db]
  (jdbc/db-do-commands 
    db
    [(jdbc/drop-table-ddl :user)
     (jdbc/drop-table-ddl :city)
     (jdbc/drop-table-ddl :category)
     (jdbc/drop-table-ddl :static_page)
     (jdbc/drop-table-ddl :navbar_item)]))

(defn table->schema-item [{:keys [tbl_name sql]}]
  [(keyword tbl_name) sql])

(defn valid-schema? [db]
  (let [query {:select [:*]
               :from   [:sqlite_master]
               :where  [:= :type "table"]}
        tables (jdbc/query db (sql/format query) {:identifiers identity})
        current-schema (select-keys (into {} (map table->schema-item tables))
                         [:user :city :category :static_page :navbar_item])
        valid-schema {:user user 
                      :city city
                      :category category
                      :static_page static-page
                      :navbar_item navbar-item}]
    (if (= valid-schema current-schema)
      true
      (do
        (log/warn "Current schema is invalid. Please correct it and restart the server.")
        false))))
