(ns co.prepacked.database.interface-ns
  (:require 
    [co.prepacked.database.core :as core]
    [co.prepacked.database.schema :as schema]))

(defn db []
  (core/db))

(defn init-database [db]
  (schema/init-database db))

(defn run-migrations [db]
  (core/run-migrations db))

(def instant->sql-timestamp core/instant->sql-timestamp)

(def ->uuid core/->uuid)

(def add-now-timestamps core/add-now-timestamps)