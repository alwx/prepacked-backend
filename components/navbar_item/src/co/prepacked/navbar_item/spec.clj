(ns co.prepacked.navbar-item.spec
  (:require 
    [co.prepacked.spec.interface-ns :as spec]
    [spec-tools.core :as st]
    [spec-tools.data-spec :as ds]))

(def add-navbar-item
  (ds/spec {:name :core/add-navbar-item
            :spec {:city_id      pos-int?
                   :title        spec/non-empty-string?
                   :priority     pos-int?
                   :content_type spec/non-empty-string?
                   :content_id   pos-int?}}))
