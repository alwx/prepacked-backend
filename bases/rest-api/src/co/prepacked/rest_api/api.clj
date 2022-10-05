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
  (GET     "/api/cities/:slug/places_lists/:places_list_slug" [] handler/places-list-with-all-dependencies)
  (POST    "/api/users/login" [] handler/login)
  (POST    "/api/users" [] handler/register))

(defroutes private-routes
  (POST    "/api/features" [] handler/add-feature)
  (PUT     "/api/features/:feature_id" [] handler/edit-feature)
  (DELETE  "/api/features/:feature_id" [] handler/delete-feature)
  (POST    "/api/places" [] handler/add-place)
  (PUT     "/api/places/:place_id" [] handler/edit-place)
  (DELETE  "/api/places/:place_id" [] handler/delete-place)
  (POST    "/api/places/:place_id/features" [] handler/add-place-feature)
  (PUT     "/api/places/:place_id/features/:feature_id" [] handler/edit-place-feature)
  (DELETE  "/api/places/:place_id/features/:feature_id" [] handler/delete-place-feature)
  (POST    "/api/cities/:slug/places-lists" [] handler/add-places-list)
  (PUT     "/api/cities/:slug/places-lists/:places_list_slug" [] handler/edit-places-list)
  (DELETE  "/api/cities/:slug/places-lists/:places_list_slug" [] handler/delete-places-list)
  (POST    "/api/cities/:slug/places-lists/:places_list_slug/places" [] handler/add-places-list-place)
  (PUT     "/api/cities/:slug/places-lists/:places_list_slug/places/:place_id" [] handler/edit-places-list-place)
  (DELETE  "/api/cities/:slug/places-lists/:places_list_slug/places/:place_id" [] handler/delete-places-list-place)

  (POST    "/api/cities/:slug/static-pages" [] handler/add-static-page)
  (PUT     "/api/cities/:slug/static-pages/:static_page_slug" [] handler/edit-static-page)
  (DELETE  "/api/cities/:slug/static-pages/:static_page_slug" [] handler/delete-static-page)
  (POST    "/api/cities/:slug/navbar-items" [] handler/add-navbar-item)
  (PUT     "/api/cities/:slug/navbar-items/:navbar_item_id" [] handler/edit-navbar-item)
  (DELETE  "/api/cities/:slug/navbar-items/:navbar_item_id" [] handler/delete-navbar-item)

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
        (do
          (log/info "Database exists.")
          (database/check-sqlite-schema db))
        (do
          (log/info "Generating database.")
          (database/generate-db db)
          (log/info "Database generated."))))
    (log/info "Initialized server.")
    (catch Exception e
      (log/error e "Could not start server."))))

(defn destroy []
  (log/info "Destroyed server."))
