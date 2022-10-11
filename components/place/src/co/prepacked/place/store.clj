(ns co.prepacked.place.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn places [city-id places-list-id]
  (let [query {:select [:place.* :places_list_place.comment]
               :from [[:place]]
               :join [[:places_list_place] [:= :place.id :places_list_place.place_id]
                      [:places_list] [:= :places_list_place.places_list_id :places_list.id]]
               :where [:and
                       [:= :places_list.city_id city-id]
                       [:= :places_list.id places-list-id]]
               :order-by [[:place.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [key value]
  (let [query {:select [:*]
               :from [:place]
               :where [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [id]
  (find-by :id id))

(defn insert-place! [place-input]
  (let [result (jdbc/insert!
                (database/db)
                :place
                place-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-place! [id place-input]
  (let [query {:update :place
               :set    place-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-place! [id]
  (let [query {:delete-from :place
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn places-list-features [places-list-id]
  (let [query {:select [:place_feature.* :feature.title :feature.icon]
               :from [[:place_feature]]
               :join [[:feature] [:= :feature.id :place_feature.feature_id]
                      [:place] [:= :place.id :place_feature.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id places-list-id]
               :order-by [[:feature.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-place-feature [place-id feature-id]
  (let [query {:select [:*]
               :from [:place_feature]
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn insert-place-feature! [input]
  (jdbc/insert! (database/db) :place_feature input))

(defn update-place-feature! [place-id feature-id place-feature-input]
  (let [query {:update :place_feature
               :set place-feature-input
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-place-feature! [place-id feature-id]
  (let [query {:delete-from :place_feature
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn places-list-files [places-list-id]
  (let [query {:select [:place_file.* :file.server_url :file.link]
               :from [[:place_file]]
               :join [[:file] [:= :file.id :place_file.file_id]
                      [:place] [:= :place.id :place_file.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id places-list-id]
               :order-by [[:place_file.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-place-file [place-id file-id]
  (let [query {:select [:*]
               :from [:place_file]
               :where [:and
                       [:= :place_id place-id]
                       [:= :file_id file-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn insert-place-file! [input]
  (jdbc/insert! (database/db) :place_file input))

(defn delete-place-file! [place-id file-id]
  (let [query {:delete-from :place_file
               :where [:and
                       [:= :place_id place-id]
                       [:= :file_id file-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))