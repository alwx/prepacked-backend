(ns co.prepacked.place.interface-ns
  (:require
   [co.prepacked.place.core :as core]))

(def places-with-all-dependencies core/places-with-all-dependencies)

(defn place-by-id [con id]
  (core/place-by-id con id))

(defn add-place! [auth-user place-input]
  (core/add-place! auth-user place-input))

(defn update-place! [place-id place-input]
  (core/update-place! place-id place-input))

(defn delete-place! [place-id]
  (core/delete-place! place-id))

(defn add-feature-to-place! [place-id input]
  (core/add-feature-to-place! place-id input))

(defn update-feature-in-place! [place-id feature-id input]
  (core/update-feature-in-place! place-id feature-id input))

(defn delete-feature-in-place! [place-id feature-id]
  (core/delete-feature-in-place! place-id feature-id))

(defn handle-file-upload! [auth-user place-id input]
  (core/handle-file-upload! auth-user place-id input))

(defn update-file-in-place! [place-id file-id input]
  (core/update-file-in-place! place-id file-id input))

(defn delete-file-in-place! [place-id file-id]
  (core/delete-file-in-place! place-id file-id))
