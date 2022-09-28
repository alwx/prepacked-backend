(ns co.prepacked.place.spec
  (:require
   [co.prepacked.spec.interface-ns :as spec]
   [spec-tools.data-spec :as ds]))

(def add-place
  (ds/spec {:name :core/add-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority pos-int?}}))

(def update-place
  (ds/spec {:name :core/update-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority pos-int?}}))
