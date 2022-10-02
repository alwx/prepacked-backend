(ns co.prepacked.place.interface-ns
  (:require
   [co.prepacked.place.core :as core]))

(defn get-places [city-id places-list-id]
  (core/get-places city-id places-list-id))

(defn place-by-id [id]
  (core/place-by-id id))

(defn add-place! [auth-user place-input]
  (core/add-place! auth-user place-input))

(defn update-place! [place-id place-input]
  (core/update-place! place-id place-input))

(defn delete-place! [place-id]
  (core/delete-place! place-id))