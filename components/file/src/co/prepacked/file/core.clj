(ns co.prepacked.file.core
  (:require [java-time]
            [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log]
            [co.prepacked.file.store :as store]))

(defn add-file! [auth-user file-data]
  (let [now (java-time/instant)
        file-data' (merge file-data
                          {:user_id (:id auth-user)
                           :created_at now})
        file-id (store/insert-file! file-data')]
    (if-let [file (store/find-by-id file-id)]
      [true file]
      [false {:errors {:other ["Cannot insert the file into the database."]}}])))