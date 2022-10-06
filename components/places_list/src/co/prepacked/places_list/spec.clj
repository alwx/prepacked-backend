(ns co.prepacked.places-list.spec
  (:require
   [co.prepacked.spec.interface-ns :as spec]
   [spec-tools.data-spec :as ds]))

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
            :spec {:place_id pos-int?
                   :comment string?}}))

(def update-place-in-places-list
  (ds/spec {:name :core/update-place-in-places-list
            :spec {:comment string?}}))
