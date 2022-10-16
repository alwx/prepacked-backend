(ns co.prepacked.places-list.spec
  (:require
   [reitit.ring.middleware.multipart :as multipart]
   [spec-tools.data-spec :as ds]
   [co.prepacked.spec.interface-ns :as spec]))

(def add-places-list
  (ds/spec {:name :core/add-places-list
            :spec {:slug spec/slug?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?}}))

(def update-places-list
  (ds/spec {:name :core/update-places-list
            :spec {:slug spec/slug?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?}}))

(def add-place-to-places-list
  (ds/spec {:name :core/add-place-to-places-list
            :spec {:place_id spec/uuid?
                   :comment string?}}))

(def update-place-in-places-list
  (ds/spec {:name :core/update-place-in-places-list
            :spec {:comment string?}}))

(def upload-file-for-places-list
  (ds/spec {:name :core/upload-file-for-places-list
            :spec {:priority int?
                   :copyright string?
                   :file multipart/temp-file-part}}))

(def update-file-in-places-list
  (ds/spec {:name :core/update-file-in-places-list
            :spec {:priority int?}}))