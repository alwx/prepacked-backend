(ns co.prepacked.places-list.interface-ns
  (:require
   [co.prepacked.places-list.core :as core]))

(defn get-places-lists [city-id]
  (core/places-lists city-id))

(defn places-list-with-all-dependencies [city-slug places-list-slug]
  (core/places-list-with-all-dependencies city-slug places-list-slug))

(defn add-places-list! [auth-user city-slug places-list-input]
  (core/add-places-list! auth-user city-slug places-list-input))

(defn update-places-list! [city-slug places-list-slug places-list-input]
  (core/update-places-list! city-slug places-list-slug places-list-input))

(defn delete-places-list! [city-slug places-list-slug]
  (core/delete-places-list! city-slug places-list-slug))

(defn add-place-to-places-list! [auth-user city-slug places-list-slug input]
  (core/add-place-to-places-list! auth-user city-slug places-list-slug input))

(defn update-place-in-places-list! [city-slug places-list-slug place-id input]
  (core/update-place-in-places-list! city-slug places-list-slug place-id input))

(defn delete-place-in-places-list! [city-slug places-list-slug place-id]
  (core/delete-place-in-places-list! city-slug places-list-slug place-id))
