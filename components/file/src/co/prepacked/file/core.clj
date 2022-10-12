(ns co.prepacked.file.core
  (:require [java-time]
            [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.file.store :as store]))

(defn file-by-id [con id]
  (store/find-by-id con id))

(defn add-file! [con auth-user file-data]
  (let [now (java-time/instant)
        file-data' (merge file-data
                          {:user_id (:id auth-user)
                           :created_at now})
        file-id (store/insert-file! con file-data')]
    (if-let [file (store/find-by-id con file-id)]
      [true file]
      [false {:errors {:other ["Cannot insert the file into the database."]} :-code 500}])))