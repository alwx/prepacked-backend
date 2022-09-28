(ns co.prepacked.places-list.spec
  (:require
   [co.prepacked.spec.interface-ns :as spec]
   [spec-tools.data-spec :as ds]))

(def add-places-list
  (ds/spec {:name :core/add-places-list
            :spec {:slug spec/slug?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority pos-int?}}))

(def update-places-list
  (ds/spec {:name :core/update-places-list
            :spec {:slug spec/slug?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority pos-int?}}))
