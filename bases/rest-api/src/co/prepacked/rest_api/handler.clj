(ns co.prepacked.rest-api.handler
  (:require 
    [clojure.edn :as edn]
    [clojure.spec.alpha :as s]
    [co.prepacked.city.interface-ns :as city]
    [co.prepacked.env.interface-ns :as env]
    [co.prepacked.spec.interface-ns :as spec]
    [co.prepacked.user.interface-ns :as user]
    [co.prepacked.user.spec :as user-spec]))

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
  (let [[ok? res] (city/cities)]
    (handle (if ok? 200 404) res)))

(defn city-with-all-dependencies [req]
  (let [slug (-> req :params :slug)]
    (if (s/valid? spec/slug? slug)
      (let [[ok? res] (city/city-with-all-dependencies slug)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:slug ["Cannot find the city."]}}))))

(defn login [req]
  (let [login-data (-> req :params)]
    (if (s/valid? user-spec/login login-data)
      (let [[ok? res] (user/login! login-data)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn register [req]
  (let [registration-data (-> req :params)]
    (if (s/valid? user-spec/register registration-data)
      (let [[ok? res] (user/register! registration-data)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn add-category [req]
  )

(defn add-static-page [req]
  )

(defn add-navbar-item [req]
  )

(defn current-user [req]
  (let [auth-user (-> req :auth-user)]
    (handle 200 {:user auth-user})))

(defn update-user [req]
  (let [auth-user (-> req :auth-user)
        user (-> req :params :user)]
    (if (s/valid? user-spec/update-user user)
      (let [[ok? res] (user/update-user! auth-user user)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))
