(ns co.prepacked.file.spec
  (:require
   [spec-tools.data-spec :as ds]))

(def update-file
  (ds/spec {:name :core/update-file
            :spec {:copyright string?}}))