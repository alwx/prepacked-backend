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

(defn add-file! [file-input]
  (core/add-file! file-input))
