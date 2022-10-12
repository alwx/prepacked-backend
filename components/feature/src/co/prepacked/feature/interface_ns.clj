(ns co.prepacked.feature.interface-ns
  (:require
   [co.prepacked.feature.core :as core]))

(defn feature-by-id [con id]
  (core/feature-by-id con id))

(defn add-feature! [feature-input]
  (core/add-feature! feature-input))

(defn update-feature! [feature-id feature-input]
  (core/update-feature! feature-id feature-input))

(defn delete-feature! [feature-id]
  (core/delete-feature! feature-id))
