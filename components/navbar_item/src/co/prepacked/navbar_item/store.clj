(ns co.prepacked.navbar-item.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn insert-navbar-item! [navbar-item-input]
  (jdbc/insert! (database/db) :navbar_item navbar-item-input))

(defn update-navbar-item! [id navbar-item-input]
  (let [query {:update :navbar_item
               :set    navbar-item-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))
