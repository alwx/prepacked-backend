(ns co.prepacked.rest-api.middleware
  (:require 
    [clojure.string :as str]
    [co.prepacked.log.interface-ns :as log]
    [co.prepacked.env.interface-ns :as env]
    [co.prepacked.user.interface-ns :as user]))

(defn wrap-auth-user [handler]
  (fn [req]
    (let [authorization (get (:headers req) "authorization")
          token (when authorization (-> (str/split authorization #" ") last))]
      (if-not (str/blank? token)
        (let [[ok? user] (user/user-by-token token)]
          (if ok?
            (handler (assoc req :auth-user (:user user)))
            (handler req)))
        (handler req)))))

(defn wrap-authorization [handler]
  (fn [req]
    (if (:auth-user req)
      (handler req)
      {:status 401
       :body   {:errors {:authorization "Authorization required."}}})))

(defn wrap-exceptions [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (let [message (str "An unknown exception occurred.")]
          (log/error e message)
          {:status 500
           :body   {:errors {:other [message]}}})))))

(defn- create-access-control-header [origin]
  (let [allowed-origins (or (env/env :allowed-origins) "")
        origins (str/split allowed-origins #",")
        allowed-origin (some #{origin} origins)]
    {"Access-Control-Allow-Origin"  allowed-origin
     "Access-Control-Allow-Methods" "POST, GET, PUT, OPTIONS, DELETE"
     "Access-Control-Max-Age"       "3600"
     "Access-Control-Allow-Headers" "Authorization, Content-Type, x-requested-with"}))

(defn wrap-cors [handler]
  (fn [req]
    (let [origin (get (:headers req) "origin")
          response (handler req)]
      (assoc response :headers (merge (:headers response) (create-access-control-header origin))))))