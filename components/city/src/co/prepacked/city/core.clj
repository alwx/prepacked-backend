(ns co.prepacked.city.core
  (:require [co.prepacked.city.store :as store]))

(defn all-cities []
  [true (store/all-cities)])

(defn city-by-slug [slug]
  (store/find-by-slug slug))
