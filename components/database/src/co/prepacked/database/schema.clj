(ns co.prepacked.database.schema
  (:require
   [clojure.java.jdbc :as jdbc]))

(def user
  [(jdbc/create-table-ddl
    :user
    [[:id :integer :primary :key :autoincrement]
     [:email :text :unique]
     [:username :text :unique]
     [:password :text]
     [:created_at :datetime]
     [:updated_at :datetime]]
    {:entities identity})])

(def city
  [(jdbc/create-table-ddl
    :city
    [[:id :integer :primary :key :autoincrement]
     [:slug :text :unique]
     [:name :text]]
    {:entities identity})
   "INSERT INTO city (slug, name) VALUES ('vienna', 'Vienna')"])

(def places-list
  [(jdbc/create-table-ddl
    :places_list
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:slug :text]
     [:title :text]
     [:description :text]
     [:created_at :datetime]
     [:updated_at :datetime]]
    {:entities identity})
   "CREATE UNIQUE INDEX idx_places_list_city_id_slug ON places_list (city_id, slug)"])

(def place
  [(jdbc/create-table-ddl
    :place
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:places_list_id :integer "references places_list(id)"]
     [:address :text]
     [:title :text]
     [:description :text]
     [:created_at :datetime]
     [:updated_at :datetime]])])

(def static-page
  [(jdbc/create-table-ddl
    :static_page
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:slug :text]
     [:title :text]
     [:content :text]
     [:created_at :datetime]
     [:updated_at :datetime]]
    {:entities identity})
   "CREATE UNIQUE INDEX idx_static_page_city_id_slug ON static_page (city_id, slug)"])

(def navbar-item
  [(jdbc/create-table-ddl
    :navbar_item
    [[:id :integer :primary :key :autoincrement]
     [:city_id :integer "references city(id)"]
     [:title :text]
     [:priority :integer]
     [:link :text]]
    {:entities identity})])

(defn generate-db [db]
  (jdbc/db-do-commands
   db
   (concat user city places-list place static-page navbar-item)))

(defn drop-db [db]
  (jdbc/db-do-commands
   db
   [(jdbc/drop-table-ddl :user)
    (jdbc/drop-table-ddl :city)
    (jdbc/drop-table-ddl :places_list)
    (jdbc/drop-table-ddl :place)
    (jdbc/drop-table-ddl :static_page)
    (jdbc/drop-table-ddl :navbar_item)]))
