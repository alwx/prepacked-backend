(ns co.prepacked.static-page.spec
  (:require 
    [co.prepacked.spec.interface-ns :as spec]
    [spec-tools.core :as st]
    [spec-tools.data-spec :as ds]))

(def add-static-page
  (ds/spec {:name :core/add-static-page
            :spec {:slug    spec/slug?
                   :title   spec/non-empty-string?
                   :content spec/non-empty-string?}}))
