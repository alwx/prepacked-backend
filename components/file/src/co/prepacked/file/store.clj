(ns co.prepacked.file.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from   [:file]
               :where  [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con id]
  (find-by con :id id))

(defn insert-file! [con file-input]
  (let [result (jdbc/insert!
                con
                :file
                file-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-file! [con id file-input]
  (let [query {:update :file
               :set    file-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-file! [con id]
  (let [query {:delete-from :file
               :where [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
