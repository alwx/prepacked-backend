(ns co.prepacked.rest-api.handler
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [co.prepacked.navbar-item.interface-ns :as navbar-item]
   [co.prepacked.navbar-item.spec :as navbar-item-spec]
   [co.prepacked.places-list.interface-ns :as places-list]
   [co.prepacked.places-list.spec :as places-list-spec]
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
  (let [[ok? res] (city/cities)]
    (handle (if ok? 200 404) res)))

(defn city-with-all-dependencies [req]
  (let [slug (-> req :params :slug)]
    (if (s/valid? spec/slug? slug)
      (let [[ok? res] (city/city-with-all-dependencies slug)]
        (handle (if ok? 200 404) res))
      (handle 422 {:errors {:slug ["Cannot find the city."]}}))))

(defn city-places-list [req]
  )

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

(defn add-places-list [req]
  (let [slug (-> req :params :slug)
        places-list-data (-> req :params :places_list)]
    (with-valid-slug slug
      (if (s/valid? places-list-spec/add-places-list places-list-data)
        (let [[ok? res] (places-list/add-places-list! slug places-list-data)]
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

(defn add-place [req])

(defn edit-place [req])

(defn delete-place [req])

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
