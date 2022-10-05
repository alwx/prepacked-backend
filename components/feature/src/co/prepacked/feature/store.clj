(ns co.prepacked.feature.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn find-by [key value]
  (let [query {:select [:*]
               :from   [:feature]
               :where  [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [id]
  (find-by :id id))

(defn insert-feature! [feature-input]
  (let [result (jdbc/insert!
                (database/db)
                :feature
                feature-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-feature! [id feature-input]
  (let [query {:update :feature
               :set    feature-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-feature! [id]
  (let [query {:delete-from :feature
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
