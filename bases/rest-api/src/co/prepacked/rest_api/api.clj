(ns co.prepacked.rest-api.api
  (:require 
    [co.prepacked.database.interface-ns :as database]
    [co.prepacked.rest-api.handler :as handler]
    [co.prepacked.rest-api.middleware :as middleware]
    [co.prepacked.log.interface-ns :as log]
    [compojure.core :refer [routes wrap-routes defroutes GET POST PUT DELETE ANY OPTIONS]]
    [ring.logger.timbre]
    [ring.middleware.json]
    [ring.middleware.keyword-params]
    [ring.middleware.multipart-params]
    [ring.middleware.nested-params]
    [ring.middleware.params]))

(defroutes public-routes
  (OPTIONS "/**" [] handler/options)
  (GET     "/api/health" [] handler/health)
  (GET     "/api/cities" [] handler/cities)
  (GET     "/api/cities/:slug" [] handler/city-with-all-dependencies)
  (POST    "/api/users/login" [] handler/login)
  (POST    "/api/users" [] handler/register))

(defroutes private-routes
  (POST    "/api/cities/:slug/categories" [] handler/add-category)
  (POST    "/api/cities/:slug/static-pages" [] handler/add-static-page)
  (POST    "/api/cities/:slug/navbar-items" [] handler/add-navbar-item)
  
  (GET     "/api/user" [] handler/current-user)
  (PUT     "/api/user" [] handler/update-user))

(defroutes other-routes
  (ANY     "/**" [] handler/other))

(def ^:private app-routes
  (routes
    (->
      private-routes
      (wrap-routes middleware/wrap-authorization)
      (wrap-routes middleware/wrap-auth-user))
    (-> 
      public-routes
      (wrap-routes middleware/wrap-auth-user))
    other-routes))

(def app
  (-> 
    app-routes
    ring.logger.timbre/wrap-with-logger
    ring.middleware.keyword-params/wrap-keyword-params
    ring.middleware.params/wrap-params
    ring.middleware.multipart-params/wrap-multipart-params
    ring.middleware.json/wrap-json-params
    ring.middleware.nested-params/wrap-nested-params
    middleware/wrap-exceptions
    ring.middleware.json/wrap-json-response
    middleware/wrap-cors))

(defn init []
  (try
    (log/init)
    (let [db (database/db)]
      (if (database/db-exists?)
        (if (database/valid-schema? db)
          (log/info "Database schema is valid.")
          (do
            (log/warn "Please fix database schema and restart")
            (System/exit 1)))
        (do
          (log/info "Generating database.")
          (database/generate-db db)
          (log/info "Database generated."))))
    (log/info "Initialized server.")
    (catch Exception e
      (log/error e "Could not start server."))))

(defn destroy []
  (log/info "Destroyed server."))
