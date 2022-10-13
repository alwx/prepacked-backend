(ns co.prepacked.file.interface-ns
  (:require [co.prepacked.file.core :as core]
            [co.prepacked.file.images :as images]))

(defn content-type->supported-ext [content-type]
  (images/content-type->supported-ext content-type))

(defn file-by-id [con id]
  (core/file-by-id con id))

(defn handle-file-upload! [con auth-user file-input db-extras]
  (core/handle-file-upload! con auth-user file-input db-extras))

(defn update-file! [file-id input]
  (core/update-file! file-id input))

(defn delete-file! [file-id]
  (core/delete-file! file-id))