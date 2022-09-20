(ns co.prepacked.static-page.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn insert-static-page! [static-page-input]
  (jdbc/insert! (database/db) :static_page static-page-input))

(defn update-static-page! [id static-page-input]
  (let [query {:update :static_page
               :set    static-page-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
