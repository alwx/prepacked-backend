(ns co.prepacked.place.interface-ns
  (:require
   [co.prepacked.place.core :as core]))

(def places-with-all-dependencies core/places-with-all-dependencies)

(def place-by-id core/place-by-id)

(def add-place! core/add-place!)

(def update-place! core/update-place!)

(def delete-place! core/delete-place!)

;; operations with `place-features`

(def add-feature-to-place! core/add-feature-to-place!)

(def update-feature-in-place! core/update-feature-in-place!)

(def delete-feature-in-place! core/delete-feature-in-place!)

;; operations with `place-files`

(def handle-file-upload! core/handle-file-upload!)

(def update-file-in-place! core/update-file-in-place!)

(def delete-file-in-place! core/delete-file-in-place!)
