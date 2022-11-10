(ns co.prepacked.rest-api.middleware
  (:require [clojure.string :as str]
            [co.prepacked.log.interface-ns :as log]
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
