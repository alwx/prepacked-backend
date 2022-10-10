(ns co.prepacked.file.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn find-by [key value]
  (let [query {:select [:*]
               :from   [:file]
               :where  [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [id]
  (find-by :id id))

(defn insert-file! [file-input]
  (let [result (jdbc/insert!
                (database/db)
                :file
                file-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-file! [id file-input]
  (let [query {:update :file
               :set    file-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-file! [id]
  (let [query {:delete-from :file
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
