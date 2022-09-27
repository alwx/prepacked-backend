(ns co.prepacked.place.interface-ns
  (:require
   [co.prepacked.place.core :as core]))

(defn get-places [city-id places-list-id]
  (core/get-places city-id places-list-id))

(defn add-place! [city-slug places-list-slug place-input]
  (core/add-place! city-slug places-list-slug place-input))

(defn update-place! [city-slug places-list-slug place-id place-input]
  (core/update-place! city-slug places-list-slug place-id place-input))

(defn delete-place! [city-slug places-list-slug place-id]
  (core/delete-place! city-slug places-list-slug place-id))