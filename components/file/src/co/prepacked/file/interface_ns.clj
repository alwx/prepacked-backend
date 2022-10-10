(ns co.prepacked.file.interface-ns
  (:require [co.prepacked.file.core :as core]))

(defn content-type->supported-ext [content-type]
  (core/content-type->supported-ext content-type))

(defn resize-image [file ext new-width]
  (core/resize-image file ext new-width))

(defn save-to-s3 [bytes filename]
  (core/save-to-s3 bytes filename))

(defn add-file! [file-input]
  (core/add-file! file-input))
