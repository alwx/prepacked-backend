(ns co.prepacked.place.interface-ns
  (:require
   [co.prepacked.place.core :as core]))

(defn places-with-all-dependencies [city-id places-list-id]
  (core/places-with-all-dependencies city-id places-list-id))

(defn place-by-id [id]
  (core/place-by-id id))

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

(defn add-image-to-place! [place-id input]
  (core/add-image-to-place! place-id input))

(defn delete-image-in-place! [place-id file-id]
  (core/delete-image-in-place! place-id file-id))