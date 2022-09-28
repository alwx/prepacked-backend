(ns co.prepacked.database.interface-ns
  (:require 
    [co.prepacked.database.core :as core]
    [co.prepacked.database.schema :as schema]))

(defn db
  ([path]
   (core/db path))
  ([]
   (core/db)))

(defn db-exists? []
  (core/db-exists?))

(defn generate-db [db]
  (schema/generate-db db))

(defn drop-db [db]
  (schema/drop-db db))

(defn check-sqlite-schema [db]
  (schema/check-sqlite-schema db))
