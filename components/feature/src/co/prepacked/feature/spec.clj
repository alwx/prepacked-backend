(ns co.prepacked.feature.spec
  (:require
   [co.prepacked.spec.interface-ns :as spec]
   [spec-tools.data-spec :as ds]))

(def add-feature
  (ds/spec {:name :core/add-feature
            :spec {:id spec/slug?
                   :title spec/non-empty-string?
                   :icon string?
                   :priority int?}}))

(def update-feature
  (ds/spec {:name :core/update-feature
            :spec {:id spec/slug?
                   :title spec/non-empty-string?
                   :icon string?
                   :priority int?}}))
