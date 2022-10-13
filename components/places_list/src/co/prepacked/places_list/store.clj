(ns co.prepacked.places-list.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]
   [co.prepacked.database.interface-ns :as database]))

(defn places-lists [con city-id]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:= :city_id [:cast city-id :uuid]]
               :order-by [[:priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn city-files [con city-id]
  (let [query {:select [:places_list_file.* :file.server_url :file.link]
               :from [[:places_list_file]]
               :join [[:file] [:= :file.id :places_list_file.file_id]
                      [:places_list] [:= :places_list.id :places_list_file.places_list_id]]
               :where [:= :places_list.city_id [:cast city-id :uuid]]
               :order-by [[:places_list_file.priority :desc]]}
        results (jdbc/query con (sql/format query))]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from [:places_list]
               :where [:and
                       [:= key value]
                       [:= :city_id [:cast city-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-slug [con city-id slug]
  (find-by con city-id :slug slug))

(defn insert-places-list! [con input]
  (let [query {:insert-into [:places_list]
               :values [(-> input
                            (select-keys [:user_id :city_id :slug :title :description :priority])
                            (update :user_id database/->uuid)
                            (update :city_id database/->uuid)
                            (database/add-now-timestamps [:created_at :updated_at]))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-places-list! [con id input]
  (let [query {:update :places_list
               :set    (-> input
                           (select-keys [:city_id :slug :title :description :priority])
                           (update :city_id database/->uuid)
                           (database/add-now-timestamps [:updated_at]))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list! [con id]
  (let [query {:delete-from :places_list
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn find-places-list-place [con places-list-id place-id]
  (let [query {:select [:*]
               :from   [:places_list_place]
               :where  [:and
                        [:= :places_list_id [:cast places-list-id :uuid]]
                        [:= :place-id [:cast place-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-places-list-place! [con input]
  (let [query {:insert-into [:places_list_place]
               :values [(-> input
                            (select-keys [:places_list_id :place_id :user_id :comment])
                            (update :places_list_id database/->uuid)
                            (update :place_id database/->uuid)
                            (update :user_id database/->uuid)
                            (database/add-now-timestamps [:created_at :updated_at]))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-places-list-place! [con places-list-id place-id input]
  (let [query {:update :places_list_place
               :set    (-> input
                           (select-keys [:comment])
                           (database/add-now-timestamps [:updated_at]))
               :where  [:and
                        [:= :places_list_id [:cast places-list-id :uuid]]
                        [:= :place-id [:cast place-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list-place! [con places-list-id place-id]
  (let [query {:delete-from :places_list_place
               :where [:and
                       [:= :places_list_id [:cast places-list-id :uuid]]
                       [:= :place-id [:cast place-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))

(defn find-places-list-file [con places-list-id file-id]
  (let [query {:select [:*]
               :from [:places_list_file]
               :where [:and
                       [:= :places_list_id [:cast places-list-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn insert-places-list-file! [con input]
  (let [query {:insert-into [:places_list_file]
               :values [(-> input
                            (select-keys [:places_list_id :file_id :priority])
                            (update :places_list_id database/->uuid)
                            (update :file_id database/->uuid))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-places-list-file! [con places-list-id file-id input]
  (let [query {:update :places_list_file
               :set (-> input
                        (select-keys [:priority]))
               :where [:and
                       [:= :places_list_id [:cast places-list-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-places-list-file! [con places-list-id file-id]
  (let [query {:delete-from :places_list_file
               :where [:and
                       [:= :places_list_id [:cast places-list-id :uuid]]
                       [:= :file_id [:cast file-id :uuid]]]}]
    (jdbc/execute! con (sql/format query))))