(ns co.prepacked.file.interface-ns
  (:require [co.prepacked.file.core :as core]
            [co.prepacked.file.images :as images]
            [co.prepacked.file.s3 :as s3]))

(defn content-type->supported-ext [content-type]
  (images/content-type->supported-ext content-type))

(defn resize-image [file ext new-width]
  (images/resize-image file ext new-width))

(defn save-to-s3 [bytes filename]
  (s3/save bytes filename))

(defn s3-public-server-url []
  (s3/s3-public-server-url))

(defn file-by-id [con id]
  (core/file-by-id con id))

(defn add-file! [con auth-user file-input]
  (core/add-file! con auth-user file-input))
