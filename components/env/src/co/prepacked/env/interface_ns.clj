(ns co.prepacked.env.interface-ns
  (:require 
    [co.prepacked.env.core :as core]))

(def env core/env)

(def get-var core/get-var)