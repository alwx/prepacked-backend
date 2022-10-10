(ns co.prepacked.file.core
  (:require [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log]
            [co.prepacked.file.store :as store]))

(defn add-file! [file-data]
  (store/insert-file! file-data)
  (if-let [file (store/find-by-id (:id file-data))]
    [true file]
    [false {:errors {:other ["Cannot insert the file into the database."]}}]))