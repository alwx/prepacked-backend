(ns co.prepacked.place.spec
  (:require
   [co.prepacked.spec.interface-ns :as spec]
   [spec-tools.data-spec :as ds]))

(def add-place
  (ds/spec {:name :core/add-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?}}))

(def update-place
  (ds/spec {:name :core/update-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?}}))

(def add-feature-to-place
  (ds/spec {:name :core/add-feature-to-place
            :spec {:feature_id spec/slug?
                   :value string?}}))

(def update-feature-in-place
  (ds/spec {:name :core/update-feature-in-place
            :spec {:value string?}}))

(def upload-file-for-place
  (ds/spec {:name :core/upload-file-for-place
            :spec {:priority int?
                   :copyright string?
                   :file any?}}))

(def update-file-in-place
  (ds/spec {:name :core/update-file-in-place
            :spec {:priority int?}}))