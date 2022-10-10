(ns co.prepacked.rest-api.handler
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [co.prepacked.place.interface-ns :as place]
   [co.prepacked.place.spec :as place-spec]
   [co.prepacked.feature.interface-ns :as feature]
   [co.prepacked.feature.spec :as feature-spec]
   [co.prepacked.file.interface-ns :as file]
   [co.prepacked.places-list.interface-ns :as places-list]
   [co.prepacked.places-list.spec :as places-list-spec]
   [co.prepacked.navbar-item.interface-ns :as navbar-item]
   [co.prepacked.navbar-item.spec :as navbar-item-spec]
   [co.prepacked.static-page.interface-ns :as static-page]
   [co.prepacked.static-page.spec :as static-page-spec]
   [co.prepacked.city.interface-ns :as city]
   [co.prepacked.env.interface-ns :as env]
   [co.prepacked.spec.interface-ns :as spec]
   [co.prepacked.user.interface-ns :as user]
   [co.prepacked.user.spec :as user-spec]))

(defn parse-query-param [param]
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

(defmacro with-valid-slug [slug body]
  `(if (s/valid? spec/slug? ~slug)
     ~body
     (handle 422 {:errors {:slug ["Invalid slug."]}})))

(defn options [_]
  (handle 200))

(defn health [_]
  (handle 200 {:environment (env/env :environment)}))

(defn other [_]
  (handle 404 {:errors {:other ["Route not found."]}}))

(defn cities [_]
  (let [[ok? res] (city/all-cities)]
    (handle (if ok? 200 404) res)))

(defn city-with-all-dependencies [req]
  (let [slug (-> req :params :slug)]
    (with-valid-slug slug
      (if-let [{:keys [id] :as city} (city/city-by-slug slug)]
        (let [city' (assoc city 
                           :places_lists (places-list/get-places-lists id)
                           :static_pages (static-page/get-static-pages id)
                           :navbar_items (navbar-item/navbar-items id))]
          (handle 200 {:city city'}))
        (handle 404 {:errors {:city ["Cannot find the city."]}})))))

(defn places-list-with-all-dependencies [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)]
    (with-valid-slug slug
      (with-valid-slug places-list-slug
        (let [[ok? res] (places-list/places-list-with-all-dependencies slug places-list-slug)]
          (handle (if ok? 200 404) res))))))

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

(defn add-feature [req]
  (let [feature-data (-> req :params :feature)]
    (if (s/valid? feature-spec/add-feature feature-data)
      (let [[ok? res] (feature/add-feature! feature-data)]
        (handle (if ok? 201 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn edit-feature [req]
  (let [feature-id (-> req :params :feature_id)
        feature-data (-> req :params :feature)]
    (with-valid-slug feature-id
      (if (s/valid? feature-spec/update-feature feature-data)
        (let [[ok? res] (feature/update-feature! feature-id feature-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-feature [req]
  (let [feature-id (-> req :params :feature_id)
        [ok? res] (feature/delete-feature! feature-id)]
    (handle (if ok? 200 404) res)))

(defn add-place [req]
  (let [auth-user (-> req :auth-user)
        place-data (-> req :params :place)]
    (if (s/valid? place-spec/add-place place-data)
      (let [[ok? res] (place/add-place! auth-user place-data)]
        (handle (if ok? 201 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn edit-place [req]
  (let [place-id (-> req :params :place_id)
        place-data (-> req :params :place)]
    (if (s/valid? place-spec/update-place place-data)
      (let [[ok? res] (place/update-place! place-id place-data)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn delete-place [req]
  (let [place-id (-> req :params :place_id)
        [ok? res] (place/delete-place! place-id)]
    (handle (if ok? 200 404) res)))

(defn add-place-feature [req]
  (let [place-id (-> req :params :place_id)
        data (-> req :params :data)]
    (if (s/valid? place-spec/add-feature-to-place data)
      (let [[ok? res] (place/add-feature-to-place! place-id data)]
        (handle (if ok? 201 404) res))
      (handle 422 {:errors {:body ["Invalid request body."]}}))))

(defn edit-place-feature [req]
  (let [place-id (-> req :params :place_id)
        feature-id (-> req :params :feature_id)
        data (-> req :params :data)]
    (with-valid-slug feature-id
      (if (s/valid? place-spec/update-feature-in-place data)
        (let [[ok? res] (place/update-feature-in-place! place-id feature-id data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-place-feature [req]
  (let [place-id (-> req :params :place_id)
        feature-id (-> req :params :feature_id)]
    (with-valid-slug feature-id
      (let [[ok? res] (place/delete-feature-in-place! place-id feature-id)]
        (handle (if ok? 200 404) res)))))

(defn add-places-list [req]
  (let [auth-user (-> req :auth-user)
        slug (-> req :params :slug)
        places-list-data (-> req :params :places_list)]
    (with-valid-slug slug
      (if (s/valid? places-list-spec/add-places-list places-list-data)
        (let [[ok? res] (places-list/add-places-list! auth-user slug places-list-data)]
          (handle (if ok? 201 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn edit-places-list [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        places-list-data (-> req :params :places_list)]
    (with-valid-slug slug
      (if (s/valid? places-list-spec/update-places-list places-list-data)
        (let [[ok? res] (places-list/update-places-list! slug places-list-slug places-list-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-places-list [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)]
    (with-valid-slug slug
      (let [[ok? res] (places-list/delete-places-list! slug places-list-slug)]
        (handle (if ok? 200 404) res)))))

(defn add-places-list-place [req]
  (let [auth-user (-> req :auth-user)
        slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        data (-> req :params :data)]
    (with-valid-slug slug
      (if (s/valid? places-list-spec/add-place-to-places-list data)
        (let [[ok? res] (places-list/add-place-to-places-list! auth-user slug places-list-slug data)]
          (handle (if ok? 201 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn edit-places-list-place [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        place-id (-> req :params :place_id)
        data (-> req :params :data)]
    (with-valid-slug slug
      (if (s/valid? places-list-spec/update-place-in-places-list data)
        (let [[ok? res] (places-list/update-place-in-places-list! slug places-list-slug place-id data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-places-list-place [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        place-id (-> req :params :place_id)]
    (with-valid-slug slug
      (let [[ok? res] (places-list/delete-place-in-places-list! slug places-list-slug place-id)]
        (handle (if ok? 200 404) res)))))

(defn add-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-data (-> req :params :static_page)]
    (with-valid-slug slug
      (if (s/valid? static-page-spec/add-static-page static-page-data)
        (let [[ok? res] (static-page/add-static-page! slug static-page-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn edit-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-slug (-> req :params :static_page_slug)
        static-page-data (-> req :params :static_page)]
    (with-valid-slug slug
      (if (s/valid? static-page-spec/update-static-page static-page-data)
        (let [[ok? res] (static-page/update-static-page! slug static-page-slug static-page-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-slug (-> req :params :static_page_slug)]
    (with-valid-slug slug
      (let [[ok? res] (static-page/delete-static-page! slug static-page-slug)]
        (handle (if ok? 200 404) res)))))

(defn add-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-data (-> req :params :navbar_item)]
    (with-valid-slug slug
      (if (s/valid? navbar-item-spec/add-navbar-item navbar-item-data)
        (let [[ok? res] (navbar-item/add-navbar-item! slug navbar-item-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn edit-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-id (-> req :params :navbar_item_id)
        navbar-item-data (-> req :params :navbar_item)]
    (with-valid-slug slug
      (if (s/valid? navbar-item-spec/update-navbar-item navbar-item-data)
        (let [[ok? res] (navbar-item/update-navbar-item! slug navbar-item-id navbar-item-data)]
          (handle (if ok? 200 404) res))
        (handle 422 {:errors {:body ["Invalid request body."]}})))))

(defn delete-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-id (-> req :params :navbar_item_id)]
    (with-valid-slug slug
      (let [[ok? res] (navbar-item/delete-navbar-item! slug navbar-item-id)]
        (handle (if ok? 200 404) res)))))

(defn post-image [req]
  (let [{:keys [content-type tempfile size]} (-> req :params :image_file)]
    (if-let [ext (file/content-type->supported-ext content-type)]
      (let [uuid (.toString (java.util.UUID/randomUUID))
            filename (format "%s.%s" uuid ext)]
        (try
          (-> (file/resize-image tempfile ext 1000)
              (file/save-to-s3 (format "images/%s" filename))) 
          (-> (file/resize-image tempfile ext 300)
              (file/save-to-s3 (format "thumbnail_images/%s" filename)))
          (handle 201 {:filename filename})
          (catch Exception e 
            (handle 422 {:errors {:image (.toString e)}}))))
      (handle 422 {:errors {:file ["Invalid file type."]}}))))

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
