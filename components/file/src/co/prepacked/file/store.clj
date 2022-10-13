(ns co.prepacked.file.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]
   [co.prepacked.database.interface-ns :as database]))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from   [:file]
               :where  [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id [:cast id :uuid]))

(defn insert-file! [con input]
  (let [query {:insert-into [:file]
               :values [(-> input
                            (select-keys [:user_id :server_url :link :copyright])
                            (update :user_id database/->uuid)
                            (database/add-now-timestamps [:created_at]))]}
        result (jdbc/execute! con (sql/format query) {:return-keys ["id"]})]
    (:id result)))

(defn update-file! [con id input]
  (let [query {:update :file
               :set    (-> input
                           (select-keys [:copyright]))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-file! [con id]
  (let [query {:delete-from :file
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))
