(ns co.prepacked.database.schema
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.set]
   [honey.sql :as sql]
   [co.prepacked.log.interface-ns :as log]))

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
     [:name :text]
     [:country_code :text]
     [:lat :real]
     [:lon :real]]
    {:entities identity})
   "INSERT INTO city (slug, name, country_code) VALUES ('any', 'Any', '', 0, 0)"
   "INSERT INTO city (slug, name, country_code) VALUES ('vienna', 'Vienna', 'at', 48.210033, 16.363449)"])

(def places-list
  [(jdbc/create-table-ddl
    :places_list
    [[:id :integer :primary :key :autoincrement]
     [:user_id :integer "references user(id)"]
     [:city_id :integer "references city(id)"]
     [:slug :text]
     [:title :text]
     [:description :text]
     [:priority :integer "default 0"]
     [:created_at :datetime]
     [:updated_at :datetime]]
    {:entities identity})
   "CREATE UNIQUE INDEX idx_places_list_city_id_slug ON places_list (city_id, slug)"])

(def place
  [(jdbc/create-table-ddl
    :place
    [[:id :integer :primary :key :autoincrement]
     [:user_id :integer "references user(id)"]
     [:address :text]
     [:title :text]
     [:description :text]
     [:priority :integer "default 0"]
     [:osm_place_id :integer]
     [:osm_lat :real]
     [:osm_lon :real]
     [:osm_amenity :text]
     [:osm_city :text]
     [:osm_city_district :text]
     [:osm_country_code :text]
     [:osm_house_number :text]
     [:osm_postcode :text]
     [:osm_road :text]
     [:osm_suburb :text]
     [:osm_display_name :text]
     [:created_at :datetime]
     [:updated_at :datetime]])])

(def places-list-place
  [(jdbc/create-table-ddl
    :places_list_place
    [[:places_list_id :integer "references places_list(id)"]
     [:place_id :integer "references place(id)"]
     [:user_id :integer "references user(id)"]
     [:comment :text]
     [:created_at :datetime]
     [:updated_at :datetime]])
   "CREATE UNIQUE INDEX idx_places_list_place ON places_list_place (places_list_id, place_id)"])

(def feature
  [(jdbc/create-table-ddl
    :feature
    [[:id :text :primary :key]
     [:title :text]
     [:icon :text]])
   "INSERT INTO feature (id, title, icon) VALUES ('wifi', 'WiFi', 'wifi')"
   "INSERT INTO feature (id, title, icon) VALUES ('laptop-policy', 'Laptop Policy', 'laptop')"
   "INSERT INTO feature (id, title, icon) VALUES ('price', 'Price', 'euro-sign')"
   "INSERT INTO feature (id, title, icon) VALUES ('location', 'Location', 'map')"
   "INSERT INTO feature (id, title, icon) VALUES ('crowd', 'Crowd', 'users')"])

(def place-feature
  [(jdbc/create-table-ddl
    :place_feature
    [[:place_id :integer "references place(id)"]
     [:feature_id :text "references feature(id)"]
     [:value :text]])
   "CREATE UNIQUE INDEX idx_place_feature ON place_feature (place_id, feature_id)"])

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
     [:priority :integer "default 0"]
     [:link :text]]
    {:entities identity})])

(defn generate-db [db]
  (jdbc/db-do-commands
   db
   (concat user city places-list place places-list-place feature place-feature static-page navbar-item)))

(defn drop-db [db]
  (jdbc/db-do-commands
   db
   [(jdbc/drop-table-ddl :user)
    (jdbc/drop-table-ddl :city)
    (jdbc/drop-table-ddl :places_list)
    (jdbc/drop-table-ddl :place)
    (jdbc/drop-table-ddl :places_list_place)
    (jdbc/drop-table-ddl :feature)
    (jdbc/drop-table-ddl :place_feature)
    (jdbc/drop-table-ddl :static_page)
    (jdbc/drop-table-ddl :navbar_item)]))

(defn- table->schema-item [{:keys [tbl_name sql]}]
  [(keyword tbl_name) sql])

(defn- map-difference [m1 m2]
  (let [ks1 (set (keys m1))
        ks2 (set (keys m2))
        ks1-ks2 (clojure.set/difference ks1 ks2)
        ks2-ks1 (clojure.set/difference ks2 ks1)
        ks1*ks2 (clojure.set/intersection ks1 ks2)]
    (merge (select-keys m1 ks1-ks2)
           (select-keys m2 ks2-ks1)
           (select-keys m1
                        (remove (fn [k] (= (m1 k) (m2 k)))
                                ks1*ks2)))))

(defn check-sqlite-schema [db]
  (let [query {:select [:*]
               :from   [:sqlite_master]
               :where  [:= :type "table"]}
        tables (jdbc/query db (sql/format query) {:identifiers identity})
        current-schema (select-keys (into {} (map table->schema-item tables))
                                    [:user 
                                     :city 
                                     :places_list 
                                     :place 
                                     :places_list_place 
                                     :feature 
                                     :place_feature 
                                     :static_page 
                                     :navbar_item])
        valid-schema {:user (first user)
                      :city (first city)
                      :places_list (first places-list)
                      :place (first place)
                      :places_list_place (first places-list-place)
                      :feature (first feature)
                      :place_feature (first place-feature)
                      :static_page (first static-page)
                      :navbar_item (first navbar-item)}] 
    (if (= valid-schema current-schema)
      true
      (do
        (log/warn "There are some differences between the expected and the actual db schema.")
        (log/warn (map-difference valid-schema current-schema))
        false))))
