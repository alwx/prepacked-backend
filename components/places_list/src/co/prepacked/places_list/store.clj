(ns co.prepacked.places-list.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]))

(defn places-lists [con city-id]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:= :city_id city-id]
               :order-by [[:priority :desc]]}
        results (jdbc/query con (sql/format query) {:identifiers identity})]
    results))

(defn city-files [con city-id]
  (let [query {:select [:places_list_file.* :file.server_url :file.link]
               :from [[:places_list_file]]
               :join [[:file] [:= :file.id :places_list_file.file_id]
                      [:places_list] [:= :places_list.id :places_list_file.places_list_id]]
               :where [:= :places_list.city_id city-id]
               :order-by [[:places_list_file.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:and
                       [:= key value]
                       [:= :city_id city-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-slug [con city-id slug]
  (find-by con city-id :slug slug))

(defn insert-places-list! [con places-list-input]
  (jdbc/insert! con :places_list places-list-input))

(defn update-places-list! [con id places-list-input]
  (let [query {:update :places_list
               :set    places-list-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list! [con id]
  (let [query {:delete-from :places_list
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn find-places-list-place [con places-list-id place-id]
  (let [query {:select [:*]
               :from   [:places_list_place]
               :where  [:and
                        [:= :places_list_id places-list-id]
                        [:= :place-id place-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-places-list-place! [con input]
  (jdbc/insert! con :places_list_place input))

(defn update-places-list-place! [con places-list-id place-id places-list-input]
  (let [query {:update :places_list_place
               :set    places-list-input
               :where  [:and
                        [:= :places_list_id places-list-id]
                        [:= :place-id place-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list-place! [con places-list-id place-id]
  (let [query {:delete-from :places_list_place
               :where [:and
                       [:= :places_list_id places-list-id]
                       [:= :place-id place-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn find-places-list-file [con places-list-id file-id]
  (let [query {:select [:*]
               :from [:places_list_file]
               :where [:and
                       [:= :places_list_id places-list-id]
                       [:= :file_id file-id]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-places-list-file! [con input]
  (jdbc/insert! con :places_list_file input))

(defn update-places-list-file! [con places-list-id file-id input]
  (let [query {:update :places_list_file
               :set input
               :where [:and
                       [:= :places_list_id places-list-id]
                       [:= :file_id file-id]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list-file! [con places-list-id file-id]
  (let [query {:delete-from :places_list_file
               :where [:and
                       [:= :places_list_id places-list-id]
                       [:= :file_id file-id]]}]
    (jdbc/execute! con (sql/format query))))