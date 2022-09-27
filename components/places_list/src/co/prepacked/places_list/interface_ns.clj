(ns co.prepacked.places-list.interface-ns
  (:require
    [co.prepacked.places-list.core :as core]))

(defn city-places-lists [city-id]
  (core/city-places-lists city-id))

(defn add-places-list! [city-slug places-list-input]
  (core/add-places-list! city-slug places-list-input))

(defn update-places-list! [city-slug places-list-slug places-list-input]
  (core/update-places-list! city-slug places-list-slug places-list-input))

(defn delete-places-list! [city-slug places-list-slug]
  (core/delete-places-list! city-slug places-list-slug))