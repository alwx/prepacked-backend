(ns co.prepacked.feature.store
  (:require
    [clojure.java.jdbc :as jdbc]
    [honey.sql :as sql]
    [co.prepacked.database.interface-ns :as database]))

(defn all-features []
  (let [query {:select [:*]
               :from   [:feature]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from   [:feature]
               :where  [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id id))

(defn insert-feature! [con input]
  (let [query {:insert-into [:feature]
               :values [(-> input
                          (select-keys [:id :title :icon]))]}
        result (jdbc/execute! con (sql/format query) {:return-keys ["id"]})]
    (:id result)))

(defn update-feature! [con id input]
  (let [query {:update :feature
               :set    (-> input
                         (select-keys [:id :title :icon]))
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-feature! [con id]
  (let [query {:delete-from :feature
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
