(ns co.prepacked.place.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]))

(defn places [con city-id places-list-id]
  (let [query {:select [:place.* :places_list_place.comment]
               :from [[:place]]
               :join [[:places_list_place] [:= :place.id :places_list_place.place_id]
                      [:places_list] [:= :places_list_place.places_list_id :places_list.id]]
               :where [:and
                       [:= :places_list.city_id city-id]
                       [:= :places_list.id places-list-id]]
               :order-by [[:place.priority :desc]]}
        results (jdbc/query con (sql/format query) {:identifiers identity})]
    results))

(defn places-list-files [con places-list-id]
  (let [query {:select [:place_file.* :file.server_url :file.link]
               :from [[:place_file]]
               :join [[:file] [:= :file.id :place_file.file_id]
                      [:place] [:= :place.id :place_file.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id places-list-id]
               :order-by [[:place_file.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from [:place]
               :where [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id id))

(defn insert-place! [con place-input]
  (let [result (jdbc/insert! con :place place-input {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-place! [con id place-input]
  (let [query {:update :place
               :set    place-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place! [con id]
  (let [query {:delete-from :place
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn places-list-features [con places-list-id]
  (let [query {:select [:place_feature.* :feature.title :feature.icon]
               :from [[:place_feature]]
               :join [[:feature] [:= :feature.id :place_feature.feature_id]
                      [:place] [:= :place.id :place_feature.place_id]
                      [:places_list_place] [:= :places_list_place.place_id :place.id]]
               :where [:= :places_list_place.places_list_id places-list-id]
               :order-by [[:feature.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-place-feature [con place-id feature-id]
  (let [query {:select [:*]
               :from [:place_feature]
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-place-feature! [con input]
  (jdbc/insert! con :place_feature input))

(defn update-place-feature! [con place-id feature-id place-feature-input]
  (let [query {:update :place_feature
               :set place-feature-input
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place-feature! [con place-id feature-id]
  (let [query {:delete-from :place_feature
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn find-place-file [con place-id file-id]
  (let [query {:select [:*]
               :from [:place_file]
               :where [:and
                       [:= :place_id place-id]
                       [:= :file_id file-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-place-file! [con input]
  (jdbc/insert! con :place_file input))

(defn update-place-file! [con place-id file-id place-file-input]
  (let [query {:update :place_file
               :set place-file-input
               :where [:and
                       [:= :place_id place-id]
                       [:= :file_id file-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-place-file! [con place-id file-id]
  (let [query {:delete-from :place_file
               :where [:and
                       [:= :place_id place-id]
                       [:= :file_id file-id]]}]
    (jdbc/execute! con (sql/format query))))
