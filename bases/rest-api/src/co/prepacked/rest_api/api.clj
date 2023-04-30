(ns co.prepacked.rest-api.api
  (:require
   [co.prepacked.rest-api.handler :as handler]
   [co.prepacked.rest-api.middleware :as middleware]
   [compojure.core :refer [routes wrap-routes defroutes GET POST PUT DELETE ANY OPTIONS]]
   [ring.logger.timbre]
   [ring.middleware.json]
   [ring.middleware.keyword-params]
   [ring.middleware.multipart-params]
   [ring.middleware.nested-params]
   [ring.middleware.params]))

(defroutes public-routes
  (OPTIONS "/**" [] handler/options)
  (GET     "/health" [] handler/health)
  (GET     "/cities" [] handler/cities)
  (GET     "/cities/:slug" [] handler/city-with-all-dependencies)
  (GET     "/cities/:slug/places-lists/:places_list_slug" [] handler/places-list-with-all-dependencies)
  (GET     "/features" [] handler/features)
  (POST    "/users/login" [] handler/login)
  (POST    "/users" [] handler/register)
  ;; TODO(alwx): all public API methods should be available under /public
  (GET     "/public/places/:slug" [] handler/get-place-by-slug))

(defroutes private-routes
  (POST    "/features" [] handler/add-feature)
  (PUT     "/features/:feature_id" [] handler/edit-feature)
  (DELETE  "/features/:feature_id" [] handler/delete-feature)
  
  (POST    "/fetch-osm-data" [] handler/fetch-osm-data)
  
  (GET     "/places" [] handler/places)
  (POST    "/places" [] handler/add-place)
  (GET     "/places/:place_id" [] handler/get-place)
  (PUT     "/places/:place_id" [] handler/edit-place)
  (DELETE  "/places/:place_id" [] handler/delete-place)
  
  (POST    "/places/:place_id/features" [] handler/add-place-feature)
  (PUT     "/places/:place_id/features/:feature_id" [] handler/edit-place-feature)
  (DELETE  "/places/:place_id/features/:feature_id" [] handler/delete-place-feature)
  
  (POST    "/places/:place_id/files" [] handler/form-upload-place-file)
  (PUT     "/places/:place_id/files/:file_id" [] handler/edit-place-file)
  (DELETE  "/places/:place_id/files/:file_id" [] handler/delete-place-file)
  
  (PUT     "/files/:file_id" [] handler/edit-file)
  (DELETE  "/files/:file_id" [] handler/delete-file)
  
  (POST    "/cities/:slug/places-lists" [] handler/add-places-list)
  (PUT     "/cities/:slug/places-lists/:places_list_slug" [] handler/edit-places-list)
  (DELETE  "/cities/:slug/places-lists/:places_list_slug" [] handler/delete-places-list)
  
  (POST    "/cities/:slug/places-lists/:places_list_slug/features" [] handler/add-places-list-feature)
  (PUT     "/cities/:slug/places-lists/:places_list_slug/features/:feature_id" [] handler/edit-places-list-feature)
  (DELETE  "/cities/:slug/places-lists/:places_list_slug/features/:feature_id" [] handler/delete-places-list-feature)
  
  (POST    "/cities/:slug/places-lists/:places_list_slug/files" [] handler/form-upload-places-list-file)
  (PUT     "/cities/:slug/places-lists/:places_list_slug/files/:file_id" [] handler/edit-places-list-file)
  (DELETE  "/cities/:slug/places-lists/:places_list_slug/files/:file_id" [] handler/delete-places-list-file)
  
  (POST    "/cities/:slug/places-lists/:places_list_slug/places" [] handler/add-places-list-place)
  (PUT     "/cities/:slug/places-lists/:places_list_slug/places/:place_id" [] handler/edit-places-list-place)
  (DELETE  "/cities/:slug/places-lists/:places_list_slug/places/:place_id" [] handler/delete-places-list-place)
  
  (POST    "/cities/:slug/static-pages" [] handler/add-static-page)
  (PUT     "/cities/:slug/static-pages/:static_page_slug" [] handler/edit-static-page)
  (DELETE  "/cities/:slug/static-pages/:static_page_slug" [] handler/delete-static-page)

  (POST    "/cities/:slug/navbar-items" [] handler/add-navbar-item)
  (PUT     "/cities/:slug/navbar-items/:navbar_item_id" [] handler/edit-navbar-item)
  (DELETE  "/cities/:slug/navbar-items/:navbar_item_id" [] handler/delete-navbar-item)

  (GET     "/user" [] handler/current-user)
  (PUT     "/user" [] handler/update-user))

(defroutes other-routes
  (ANY     "/**" [] handler/not-found))

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
