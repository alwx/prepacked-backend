(ns co.prepacked.place.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]
   [co.prepacked.database.interface-ns :as database]))

(defn all-places []
  (let [query {:select [:place.*]
               :from   [:place]
               :order-by [[:place.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn places [con city-id places-list-id]
  (let [query {:select [:place.* :places_list_place.comment]
               :from [[:place]]
               :join [[:places_list_place] [:= :place.id :places_list_place.place_id]
                      [:places_list] [:= :places_list_place.places_list_id :places_list.id]]
               :where [:and
                       [:= :places_list.city_id [:cast city-id :uuid]]
                       [:= :places_list.id [:cast places-list-id :uuid]]]
               :order-by [[:place.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from [:place]
               :where [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id [:cast id :uuid]))

(def osm-keys [:osm_place_id :osm_lat :osm_lon :osm_amenity :osm_city :osm_city_district :osm_country_code 
               :osm_house_number :osm_postcode :osm_road :osm_suburb :osm_display_name])

(defn insert-place! [con input]
  (let [query {:insert-into [:place]
               :values [(-> input
                            (select-keys (into [:user_id :address :title :description :priority] 
                                               osm-keys))
                            (update :user_id database/->uuid)
                            (database/add-now-timestamps [:created_at :updated_at]))]}
        result (jdbc/execute! con (sql/format query) {:return-keys ["id"]})]
    (:id result)))

(defn update-place! [con id input]
  (let [query {:update :place
               :set    (-> input
                           (select-keys (into [:address :title :description :priority]
                                              osm-keys))
                           (database/add-now-timestamps [:updated_at]))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place! [con id]
  (let [query {:delete-from :place
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

;; features

(defn places-list-features [con places-list-id]
  (let [query {:select [:place_feature.* :feature.title :feature.icon]
               :from [[:place_feature]]
               :join [[:feature] [:= :feature.id :place_feature.feature_id]
                      [:place] [:= :place.id :place_feature.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id [:cast places-list-id :uuid]]
               :order-by [[:place_feature.priority :desc] [:place_feature.created_at :asc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn place-features [con place-id]
  (let [query {:select [:place_feature.* :feature.title :feature.icon]
               :from [[:place_feature]]
               :join [[:feature] [:= :feature.id :place_feature.feature_id]]
               :where [:= :place_feature.place_id [:cast place-id :uuid]]
               :order-by [[:place_feature.priority :desc] [:place_feature.created_at :asc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-place-feature [con id]
  (let [query {:select [:*]
               :from [:place_feature]
               :where [:= :id [:cast id :uuid]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-place-feature! [con input]
  (let [query {:insert-into [:place_feature]
               :values [(-> input
                            (select-keys [:place_id :feature_id :value :priority])
                            (update :place_id database/->uuid)
                            (database/add-now-timestamps [:created_at :updated_at]))]}
        result (jdbc/execute! con (sql/format query) {:return-keys ["id"]})]
    (:id result)))

(defn update-place-feature! [con id input]
  (let [query {:update :place_feature
               :set (-> input
                        (select-keys [:value :priority])
                        (database/add-now-timestamps [:updated_at]))
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place-feature! [con id]
  (let [query {:delete-from :place_feature
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

;; files

(defn places-list-files [con places-list-id]
  (let [query {:select [:place_file.* :file.server_url :file.link :file.copyright]
               :from [[:place_file]]
               :join [[:file] [:= :file.id :place_file.file_id]
                      [:place] [:= :place.id :place_file.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id [:cast places-list-id :uuid]]
               :order-by [[:place_file.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn place-files [con place-id]
  (let [query {:select [:place_file.* :file.server_url :file.link :file.copyright]
               :from [[:place_file]]
               :join [[:file] [:= :file.id :place_file.file_id]]
               :where [:= :place_file.place_id [:cast place-id :uuid]]
               :order-by [[:place_file.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-place-file [con place-id file-id]
  (let [query {:select [:*]
               :from [:place_file]
               :where [:and
                       [:= :place_id [:cast place-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-place-file! [con input]
  (let [query {:insert-into [:place_file]
               :values [(-> input
                            (select-keys [:place_id :file_id :priority])
                            (update :place_id database/->uuid)
                            (update :file_id database/->uuid))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-place-file! [con place-id file-id input]
  (let [query {:update :place_file
               :set (-> input
                        (select-keys [:priority]))
               :where [:and
                       [:= :place_id [:cast place-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place-file! [con place-id file-id]
  (let [query {:delete-from :place_file
               :where [:and
                       [:= :place_id [:cast place-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))
