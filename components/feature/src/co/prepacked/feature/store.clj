(ns co.prepacked.feature.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from   [:feature]
               :where  [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id id))

(defn insert-feature! [con feature-input]
  (let [result (jdbc/insert! con :feature feature-input {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-feature! [con id feature-input]
  (let [query {:update :feature
               :set    feature-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-feature! [con id]
  (let [query {:delete-from :feature
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
