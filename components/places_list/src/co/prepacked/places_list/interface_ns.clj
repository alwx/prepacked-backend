(ns co.prepacked.places-list.interface-ns
  (:require
   [co.prepacked.places-list.core :as core]))

;; operations with `places-lists`

(def places-lists-with-all-dependencies core/places-lists-with-all-dependencies)

(def places-list-with-all-dependencies core/places-list-with-all-dependencies)

(def add-places-list! core/add-places-list!)

(def update-places-list! core/update-places-list!)

(def delete-places-list! core/delete-places-list!)

;; operations with `places-list-features`

(def add-feature-to-places-list! core/add-feature-to-places-list!)

(def update-feature-in-places-list! core/update-feature-in-places-list!)

(def delete-feature-in-places-list! core/delete-feature-in-places-list!)

;; operations with `places-list-files`

(def handle-file-upload! core/handle-file-upload!)

(def update-file-in-places-list! core/update-file-in-places-list!)

(def delete-file-in-places-list! core/delete-file-in-places-list!)

;; operations with `places-list-places`

(def add-place-to-places-list! core/add-place-to-places-list!)

(def update-place-in-places-list! core/update-place-in-places-list!)

(def delete-place-in-places-list! core/delete-place-in-places-list!)
