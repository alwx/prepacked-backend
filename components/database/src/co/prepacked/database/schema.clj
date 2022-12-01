(ns co.prepacked.database.schema
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.set]))

(def initial
  ["CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"])

(def app-user
  [(jdbc/create-table-ddl
    :app_user
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:email :text :unique]
     [:username :text :unique]
     [:password :text]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})])

(def city
  [(jdbc/create-table-ddl
    :city
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:slug :text :unique]
     [:name :text]
     [:country_code :text]
     [:lat :real]
     [:lon :real]]
    {:conditional? true})])

(def feature
  [(jdbc/create-table-ddl
    :feature
    [[:id :text :primary :key]
     [:title :text]
     [:icon :text]]
    {:conditional? true})])

(def file
  [(jdbc/create-table-ddl
    :file
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:user_id :uuid "REFERENCES app_user(id)"]
     [:server_url :text]
     [:link :text]
     [:copyright :text]
     [:created_at :timestamp]]
    {:conditional? true})])

(def place
  [(jdbc/create-table-ddl
    :place
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:user_id :uuid "REFERENCES app_user(id)"]
     [:address :text]
     [:title :text]
     [:description :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
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
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})])

(def place-feature
  [(jdbc/create-table-ddl
    :place_feature
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:place_id :uuid "REFERENCES place(id)"]
     [:feature_id :text "REFERENCES feature(id)"]
     [:value :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})])

(def place-file
  [(jdbc/create-table-ddl
    :place_file
    [[:place_id :uuid "REFERENCES place(id)"]
     [:file_id :uuid "REFERENCES file(id)"]
     [:priority :integer "DEFAULT 0 NOT NULL"]]
    {:conditional? true})
   "CREATE UNIQUE INDEX IF NOT EXISTS idx_place_file ON place_file (place_id, file_id)"])

(def places-list
  [(jdbc/create-table-ddl
    :places_list
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:user_id :uuid "REFERENCES app_user(id)"]
     [:city_id :uuid "REFERENCES city(id)"]
     [:slug :text]
     [:title :text]
     [:description :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
     [:shown_features :jsonb "DEFAULT '[]'::jsonb"]
     [:tags :jsonb "DEFAULT '[]'::jsonb"]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})
   "CREATE UNIQUE INDEX IF NOT EXISTS idx_places_list_city_id_slug ON places_list (city_id, slug)"])

(def places-list-feature
  [(jdbc/create-table-ddl
    :places_list_feature
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:places_list_id :uuid "REFERENCES places_list(id)"]
     [:feature_id :text "REFERENCES feature(id)"]
     [:value :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})])

(def places-list-file
  [(jdbc/create-table-ddl
    :places_list_file
    [[:places_list_id :uuid "REFERENCES places_list(id)"]
     [:file_id :uuid "REFERENCES file(id)"]
     [:priority :integer "DEFAULT 0 NOT NULL"]]
    {:conditional? true})
   "CREATE UNIQUE INDEX IF NOT EXISTS idx_places_list_file ON places_list_file (places_list_id, file_id)"])

(def places-list-place
  [(jdbc/create-table-ddl
    :places_list_place
    [[:places_list_id :uuid "REFERENCES places_list(id)"]
     [:place_id :uuid "REFERENCES place(id)"]
     [:user_id :uuid "REFERENCES app_user(id)"]
     [:comment :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})
   "CREATE UNIQUE INDEX IF NOT EXISTS idx_places_list_place ON places_list_place (places_list_id, place_id)"])

(def static-page
  [(jdbc/create-table-ddl
    :static_page
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:city_id :uuid "REFERENCES city(id)"]
     [:slug :text]
     [:title :text]
     [:content :text]
     [:created_at :timestamp]
     [:updated_at :timestamp]]
    {:conditional? true})
   "CREATE UNIQUE INDEX IF NOT EXISTS idx_static_page_city_id_slug ON static_page (city_id, slug)"])

(def navbar-item
  [(jdbc/create-table-ddl
    :navbar_item
    [[:id :uuid :primary :key "DEFAULT uuid_generate_v4()"]
     [:city_id :uuid "REFERENCES city(id)"]
     [:title :text]
     [:priority :integer "DEFAULT 0 NOT NULL"]
     [:link :text]]
    {:conditional? true})])

(defn init-database [db]
  (jdbc/db-do-commands
   db
   (concat initial
           app-user
           city
           feature
           file
           place
           place-feature
           place-file
           places-list
           places-list-feature
           places-list-file
           places-list-place
           static-page
           navbar-item)))
