(ns co.prepacked.category.spec
  (:require 
    [co.prepacked.spec.interface-ns :as spec]
    [spec-tools.core :as st]
    [spec-tools.data-spec :as ds]))

(def add-category
  (ds/spec {:name :core/add-category
            :spec {:slug        spec/slug?
                   :title       spec/non-empty-string?
                   :description spec/non-empty-string?}}))
