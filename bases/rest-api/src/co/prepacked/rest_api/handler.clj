(ns co.prepacked.rest-api.handler
  (:require 
    [clojure.edn :as edn]
    [clojure.spec.alpha :as s]
    [co.prepacked.city.interface-ns :as city]
    [co.prepacked.env.interface-ns :as env]
    [co.prepacked.spec.interface-ns :as spec]))

(defn- parse-query-param [param]
  (if (string? param)
    (try
      (edn/read-string param)
      (catch Exception _
        param))
    param))

(defn- handle
  ([status body]
   {:status (or status 404)
    :body   body})
  ([status]
   (handle status nil)))

(defn options [_]
  (handle 200))

(defn health [_]
  (handle 200 {:environment (env/env :environment)}))

(defn other [_]
  (handle 404 {:errors {:other ["Route not found."]}}))

(defn cities [_]
  (let [[ok? res] (city/all-cities)]
    (handle (if ok? 200 404) res)))