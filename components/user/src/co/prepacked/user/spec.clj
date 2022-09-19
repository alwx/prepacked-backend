(ns co.prepacked.user.spec
  (:require 
    [co.prepacked.spec.interface-ns :as spec]
    [spec-tools.core :as st]
    [spec-tools.data-spec :as ds]))

(def id
  (st/spec {:spec        pos-int?
            :type        :long
            :description "A long spec that defines a user id which is a positive integer"}))

(def login
  (ds/spec {:name :core/login
            :spec {:email    spec/email?
                   :password spec/password?}}))

(def register
  (ds/spec {:name :core/register
            :spec {:username spec/username?
                   :email    spec/email?
                   :password spec/password?}}))

(def update-user
  (ds/spec {:name :core/update-user
            :spec {:email    spec/email?
                   :username spec/username?
                   :password spec/password?}
            :keys-default ds/opt}))

(def user-base
  {:id             id
   :email          spec/email?
   :username       spec/username?})

(def user
  (ds/spec {:name :core/user
            :spec user-base}))

(def visible-user
  (ds/spec {:name :core/visible-user
            :spec {:user (assoc user-base
                           (ds/opt :token) spec/non-empty-string?)}}))
