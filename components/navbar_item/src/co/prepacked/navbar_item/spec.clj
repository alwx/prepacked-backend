(ns co.prepacked.navbar-item.spec
  (:require 
    [co.prepacked.spec.interface-ns :as spec]
    [spec-tools.data-spec :as ds]))

(def add-navbar-item
  (ds/spec {:name :core/add-navbar-item
            :spec {:title spec/non-empty-string?
                   :priority pos-int?
                   :link spec/non-empty-string?}}))

(def update-navbar-item
  (ds/spec {:name :core/update-navbar-item
            :spec {:title spec/non-empty-string?
                   :priority pos-int?
                   :link spec/non-empty-string?}}))
