(ns co.prepacked.rest-api.api
  (:require 
    [clojure.data.json :as json]
    [muuntaja.core]
    [reitit.ring :as ring]
    [reitit.coercion.spec]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.dev.pretty :as pretty]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.logger.timbre]
    [ring.middleware.cors :refer [wrap-cors]]
    [co.prepacked.rest-api.handler :as handler]
    [co.prepacked.rest-api.middleware :as middleware]
    [co.prepacked.feature.spec :as feature-spec]
    [co.prepacked.file.spec :as file-spec]
    [co.prepacked.navbar-item.spec :as navbar-item-spec]
    [co.prepacked.place.spec :as place-spec]
    [co.prepacked.places-list.spec :as places-list-spec]
    [co.prepacked.static-page.spec :as static-page-spec]
    [co.prepacked.user.spec :as user-spec]
    [co.prepacked.spec.interface-ns :as spec]))

(def api-routes
  [""
   {:swagger {:id :prepacked-api}}
   
   ["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "Prepacked API"
                            :description "alwxdev.com"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/health"
    {:swagger {:tags ["health"]}
     :get {:summary "current state of the API"
           :responses {200 {:body {:environment string?}}}
           :handler handler/health}}]

   ["/users" {:swagger {:tags ["users"]}}
    [""
     {:post {:summary "create/register a new user"
             :parameters {:body user-spec/register}
             :responses {200 {:body user-spec/visible-user}}
             :handler handler/register}}]
    ["/login"
     {:post {:summary "performs a login"
             :parameters {:body user-spec/login}
             :responses {200 {:body user-spec/visible-user}}
             :handler handler/login}}]]

   ["/cities" {:swagger {:tags ["cities"]}}
    [""
     {:get {:summary "gets all cities"
            :handler handler/cities}}]
    ["/:slug"
     [""
      {:get {:summary "detailed information about a specific city"
             :parameters {:path {:slug spec/slug?}}
             :handler handler/city-with-all-dependencies}}]
     ["/places-lists"
      [""
       {:post {:summary "add a places list"
               :parameters {:body places-list-spec/add-places-list
                            :path {:slug spec/slug?}
                            :header {:authorization string?}}
               :middleware [middleware/wrap-authorization]
               :handler handler/add-places-list}}]
      ["/:places_list_slug"
       [""
        {:parameters {:path {:slug spec/slug?
                             :places_list_slug spec/slug?}}
         :get {:summary "detailed information about a specific list of places within a city"
               :handler handler/places-list-with-all-dependencies}
         :put {:summary "update a places list"
               :parameters {:body places-list-spec/update-places-list
                            :header {:authorization string?}}
               :middleware [middleware/wrap-authorization]
               :handler handler/edit-places-list}
         :delete {:summary "delete a places list"
                  :parameters {:header {:authorization string?}}
                  :middleware [middleware/wrap-authorization]
                  :handler handler/delete-places-list}}]
       ["/files" {:middleware [middleware/wrap-authorization]}
        [""
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?}
                       :header {:authorization string?}}
          :post {:summary "add a file (e.g. an image) to a specified list of places"
                 :parameters {:multipart places-list-spec/upload-file-for-places-list}
                 :handler handler/form-upload-places-list-file}}]
        ["/:file_id"
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?
                              :file_id spec/uuid?}
                       :header {:authorization string?}}
          :put {:summary "update a file in a specified list of places"
                :parameters {:body places-list-spec/update-file-in-places-list}
                :handler handler/edit-places-list-file}
          :delete {:summary "deletes a file from a specified list of places"
                   :handler handler/delete-places-list-file}}]]
       ["/features" {:middleware [middleware/wrap-authorization]}
        [""
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?}
                       :header {:authorization string?}}
          :post {:summary "add a feature to a specified list of places"
                 :parameters {:body places-list-spec/add-feature-to-places-list}
                 :handler handler/add-places-list-feature}}]
        ["/:id"
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?
                              :id spec/uuid?}
                       :header {:authorization string?}}
          :put {:summary "update a feature in a specified list of places"
                :parameters {:body places-list-spec/update-feature-in-places-list}
                :handler handler/edit-places-list-feature}
          :delete {:summary "deletes a feature from a specified list of places"
                   :handler handler/delete-places-list-feature}}]]
       

       ["/places" {:middleware [middleware/wrap-authorization]}
        [""
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?}
                       :header {:authorization string?}}
          :post {:summary "add a place to a specified list of places"
                 :parameters {:body places-list-spec/add-place-to-places-list}
                 :handler handler/add-places-list-place}}]
        ["/:place_id"
         {:parameters {:path {:slug spec/slug?
                              :places_list_slug spec/slug?
                              :place_id spec/uuid?}
                       :header {:authorization string?}}
          :put {:summary "update a place in a specified list of places"
                :parameters {:body places-list-spec/update-place-in-places-list}
                :handler handler/edit-places-list-place}
          :delete {:summary "deletes a place from a specified list of places"
                   :handler handler/delete-places-list-place}}]]]]

     ["/static-pages" {:middleware [middleware/wrap-authorization]}
      [""
       {:post {:summary "add a static page"
               :parameters {:body static-page-spec/add-static-page
                            :header {:authorization string?}}
               :handler handler/add-static-page}}]
      ["/:static_page_slug"
       {:parameters {:path {:slug spec/slug?
                            :static_page_slug spec/slug?}
                     :header {:authorization string?}}
        :put {:summary "update a static page"
              :parameters {:body static-page-spec/update-static-page}
              :handler handler/edit-static-page}
        :delete {:summary "delete a static page"
                 :handler handler/delete-static-page}}]]
     
     ["/navbar-items" {:middleware [middleware/wrap-authorization]}
      [""
       {:post {:summary "add a navbar item"
               :parameters {:body navbar-item-spec/add-navbar-item
                            :header {:authorization string?}}
               :handler handler/add-navbar-item}}]
      ["/:navbar_item_id"
       {:parameters {:path {:slug spec/slug?
                            :navbar_item_id spec/uuid?}
                     :header {:authorization string?}}
        :put {:summary "update a navbar item"
              :parameters {:body navbar-item-spec/update-navbar-item}
              :handler handler/edit-navbar-item}
        :delete {:summary "delete a navbar item"
                 :handler handler/delete-navbar-item}}]]]]

   ["/features" {:middleware [middleware/wrap-authorization]
                 :parameters {:header {:authorization string?}}
                 :swagger {:tags ["features"]}}
    [""
     {:get {:summary "get all features"
            :handler handler/features}
      :post {:summary "create a new feature"
             :parameters {:body feature-spec/add-feature}
             :handler handler/add-feature}}]
    ["/:feature_id"
     {:put {:summary "update a feature"
            :parameters {:path {:feature_id spec/slug?}
                         :body feature-spec/update-feature}
            :handler handler/edit-feature}
      :delete {:summary "delete a feature"
               :parameters {:path {:feature_id spec/slug?}}
               :handler handler/delete-feature}}]]

   ["/fetch-osm-data" {:middleware [middleware/wrap-authorization]
     :parameters {:header {:authorization string?}}
     :swagger {:tags ["places"]}}
    [""
     {:post {:summary "sends a request to OSM provider to fetch data about a specific address"
            :parameters {:body place-spec/fetch-osm-data}
            :handler handler/fetch-osm-data}}]]
   
   ["/places" 
    {:middleware [middleware/wrap-authorization]
     :parameters {:header {:authorization string?}}
     :swagger {:tags ["places"]}}
    [""
     {:get {:summary "get all places"
            :handler handler/places}
      :post {:summary "create a new place"
             :parameters {:body place-spec/add-place}
             :handler handler/add-place}}]
    ["/:place_id"
     {:parameters {:path {:place_id spec/uuid?}}
      :get {:summary "get a place with all the dependencies (features, files, etc)"
            :handler handler/get-place}
      :put {:summary "update a place"
            :parameters {:body place-spec/update-place}
            :handler handler/edit-place}
      :delete {:summary "delete a place"
               :handler handler/delete-place}}]

    ["/:place_id/features"
     [""
      {:parameters {:path {:place_id spec/uuid?}}
       :post {:summary "add a feature to a place"
              :parameters {:body place-spec/add-feature-to-place}
              :handler handler/add-place-feature}}]
     ["/:id"
      {:parameters {:path {:place_id spec/uuid?
                           :id spec/uuid?}}
       :put {:summary "update a feature in a place"
             :parameters {:body place-spec/update-feature-in-place}
             :handler handler/edit-place-feature}
       :delete {:summary "delete a feature from a place"
                :handler handler/delete-place-feature}}]] 

    ["/:place_id/files"
     [""
      {:parameters {:path {:place_id spec/uuid?}}
       :post {:summary "add a file (e.g. an image) to a place"
              :parameters {:multipart place-spec/upload-file-for-place}
              :handler handler/form-upload-place-file}}]
     ["/:file_id"
      {:parameters {:path {:place_id spec/uuid?
                           :file_id spec/uuid?}}
       :put {:summary "update a file in a place"
             :parameters {:body place-spec/update-file-in-place}
             :handler handler/edit-place-file}
       :delete {:summary "delete a file from a place"
                :handler handler/delete-place-file}}]]]

   ["/files" {:middleware [middleware/wrap-authorization]
              :parameters {:header {:authorization string?}}
              :swagger {:tags ["files"]}}
    ["/:file_id"
     {:parameters {:path {:file_id spec/uuid?}}
      :put {:summary "update some file information"
            :parameters {:body file-spec/update-file}
            :handler handler/edit-file}
      :delete {:summary "delete a file"
               :handler handler/delete-file}}]]
   
   ["/user" {:middleware [middleware/wrap-authorization]
             :parameters {:header {:authorization string?}}
             :swagger {:tags ["user"]}}
    ["" 
     {:get {:summary "get current user info"
            :handler handler/current-user}
      :put {:summary "update current user"
            :parameters {:body user-spec/update-user}
            :handler handler/update-user}}]]])

(def ^:private exception-middleware
  (exception/create-exception-middleware
    (merge
      exception/default-handlers
      {::exception/wrap (fn [handler exception request]
                          (assoc (handler exception request) :body json/write-str))})))

(def ^:private cors-middleware
  [wrap-cors
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:delete :get :patch :put :post]])

(def app
  (ring/ring-handler
    (ring/router
      [api-routes]
      {:exception pretty/exception
       :data {:coercion reitit.coercion.spec/coercion
              :muuntaja muuntaja.core/instance
              :middleware [cors-middleware
                           ring.logger.timbre/wrap-with-logger
                           reitit.swagger/swagger-feature
                           parameters/parameters-middleware
                           muuntaja/format-middleware
                           exception-middleware
                           coercion/coerce-request-middleware
                           multipart/multipart-middleware
                           middleware/wrap-auth-user]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path "/api-docs"
         :url "../swagger.json"
         :config {:validatorUrl nil
                  :operationsSorter "alpha"}})
      (ring/create-default-handler
        {:not-found handler/not-found}))))
