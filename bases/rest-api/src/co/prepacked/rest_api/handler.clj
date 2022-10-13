(ns co.prepacked.rest-api.handler
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [co.prepacked.city.interface-ns :as city]
   [co.prepacked.env.interface-ns :as env]
   [co.prepacked.feature.interface-ns :as feature]
   [co.prepacked.feature.spec :as feature-spec]
   [co.prepacked.file.interface-ns :as file]
   [co.prepacked.file.spec :as file-spec]
   [co.prepacked.navbar-item.interface-ns :as navbar-item]
   [co.prepacked.navbar-item.spec :as navbar-item-spec]
   [co.prepacked.place.interface-ns :as place]
   [co.prepacked.place.spec :as place-spec]
   [co.prepacked.places-list.interface-ns :as places-list]
   [co.prepacked.places-list.spec :as places-list-spec]
   [co.prepacked.static-page.interface-ns :as static-page]
   [co.prepacked.static-page.spec :as static-page-spec]
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
   {:status status
    :body   body})
  ([status]
   (handle status nil)))

(defn- handle-result [res]
  {:status (or (:-code res) 200)
   :body (dissoc res :-code)})

(defn- handle-invalid-spec []
  (handle 422 {:errors {:body ["Invalid request body."]}}))

(defn options [_]
  (handle 200))

(defn health [_]
  (handle 200 {:environment (env/env :environment)}))

(defn other [_]
  (handle 404 {:errors {:other ["Route not found."]}}))

(defn cities [_]
  (handle 200 (city/all-cities)))

(defn city-with-all-dependencies [req]
  (let [slug (-> req :params :slug)]
    (if-let [{:keys [id] :as city} (city/city-by-slug slug)]
      (let [city' (assoc city
                         :places_lists (places-list/places-lists-with-all-dependencies id)
                         :static_pages (static-page/static-pages id)
                         :navbar_items (navbar-item/navbar-items id))]
        (handle 200 {:city city'}))
      (handle 404 {:errors {:city ["Cannot find the city."]}}))))

(defn places-list-with-all-dependencies [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        [_ res] (places-list/places-list-with-all-dependencies slug places-list-slug)]
    (handle-result res)))

(defn login [req]
  (let [login-data (-> req :params)]
    (if (s/valid? user-spec/login login-data)
      (let [[_ res] (user/login! login-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn register [req]
  (let [registration-data (-> req :params)]
    (if (s/valid? user-spec/register registration-data)
      (let [[_ res] (user/register! registration-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn add-feature [req]
  (let [feature-data (-> req :params :feature)]
    (if (s/valid? feature-spec/add-feature feature-data)
      (let [[_ res] (feature/add-feature! feature-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-feature [req]
  (let [feature-id (-> req :params :feature_id)
        feature-data (-> req :params :feature)]
    (if (s/valid? feature-spec/update-feature feature-data)
      (let [[_ res] (feature/update-feature! feature-id feature-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-feature [req]
  (let [feature-id (-> req :params :feature_id)
        [_ res] (feature/delete-feature! feature-id)]
    (handle-result res)))

(defn add-place [req]
  (let [auth-user (-> req :auth-user)
        place (-> req :params :place)]
    (if (s/valid? place-spec/add-place place)
      (let [[_ res] (place/add-place! auth-user place)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-place [req]
  (let [place-id (-> req :params :place_id)
        place (-> req :params :place)]
    (if (s/valid? place-spec/update-place place)
      (let [[_ res] (place/update-place! place-id place)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-place [req]
  (let [place-id (-> req :params :place_id)
        [_ res] (place/delete-place! place-id)]
    (handle-result res)))

(defn add-place-feature [req]
  (let [place-id (-> req :params :place_id)
        place-feature (-> req :params :place_feature)]
    (if (s/valid? place-spec/add-feature-to-place place-feature)
      (let [[_ res] (place/add-feature-to-place! place-id place-feature)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-place-feature [req]
  (let [place-id (-> req :params :place_id)
        feature-id (-> req :params :feature_id)
        place-feature (-> req :params :place_feature)]
    (if (s/valid? place-spec/update-feature-in-place place-feature)
      (let [[_ res] (place/update-feature-in-place! place-id feature-id place-feature)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-place-feature [req]
  (let [place-id (-> req :params :place_id)
        feature-id (-> req :params :feature_id)
        [_ res] (place/delete-feature-in-place! place-id feature-id)]
    (handle-result res)))

(defn edit-file [req]
  (let [file-id (-> req :params :file_id)
        file-data (-> req :params :file)]
    (if (s/valid? file-spec/update-file file-data)
      (let [[_ res] (file/update-file! file-id file-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-file [req]
  (let [file-id (-> req :params :file_id)
        [_ res] (file/delete-file! file-id)]
    (handle-result res)))

(defn form-upload-place-file [req]
  ;; this one is special because it expects form data, not a JSON
  (let [place-id (-> req :params :place_id)
        auth-user (-> req :auth-user)
        params (-> req :params (update :priority parse-query-param))]
    (if (s/valid? place-spec/upload-file-for-place params)
      (let [[_ res] (place/handle-file-upload! auth-user place-id params)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-place-file [req]
  (let [place-id (-> req :params :place_id)
        file-id (-> req :params :file_id)
        place-file (-> req :params :place_file)]
    (if (s/valid? place-spec/update-file-in-place place-file)
      (let [[_ res] (place/update-file-in-place! place-id file-id place-file)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-place-file [req]
  (let [place-id (-> req :params :place_id)
        file-id (-> req :params :file_id)
        [_ res] (place/delete-file-in-place! place-id file-id)]
    (handle-result res)))

(defn add-places-list [req]
  (let [auth-user (-> req :auth-user)
        slug (-> req :params :slug)
        places-list-data (-> req :params :places_list)]
    (if (s/valid? places-list-spec/add-places-list places-list-data)
      (let [[res] (places-list/add-places-list! auth-user slug places-list-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-places-list [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        places-list-data (-> req :params :places_list)]
    (if (s/valid? places-list-spec/update-places-list places-list-data)
      (let [[_ res] (places-list/update-places-list! slug places-list-slug places-list-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-places-list [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        [_ res] (places-list/delete-places-list! slug places-list-slug)]
    (handle-result res)))

(defn add-places-list-place [req]
  (let [auth-user (-> req :auth-user)
        slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        places-list-place (-> req :params :places_list_place)]
    (if (s/valid? places-list-spec/add-place-to-places-list places-list-place)
      (let [[_ res] (places-list/add-place-to-places-list! auth-user slug places-list-slug places-list-place)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-places-list-place [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        place-id (-> req :params :place_id)
        places-list-place (-> req :params :places_list_place)]
    (if (s/valid? places-list-spec/update-place-in-places-list places-list-place)
      (let [[_ res] (places-list/update-place-in-places-list! slug places-list-slug place-id places-list-place)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-places-list-place [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        place-id (-> req :params :place_id)
        [_ res] (places-list/delete-place-in-places-list! slug places-list-slug place-id)]
    (handle-result res)))

(defn form-upload-places-list-file [req]
  ;; this one is special because it expects form data, not a JSON
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        auth-user (-> req :auth-user)
        params (-> req :params (update :priority parse-query-param))]
    (if (s/valid? places-list-spec/upload-file-for-places-list params)
      (let [[_ res] (places-list/handle-file-upload! auth-user slug places-list-slug params)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-places-list-file [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        file-id (-> req :params :file_id)
        place-file (-> req :params :place_file)]
    (if (s/valid? place-spec/update-file-in-place place-file)
      (let [[_ res] (places-list/update-file-in-places-list! slug places-list-slug file-id place-file)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-places-list-file [req]
  (let [slug (-> req :params :slug)
        places-list-slug (-> req :params :places_list_slug)
        file-id (-> req :params :file_id)
        [_ res] (places-list/delete-file-in-places-list! slug places-list-slug file-id)]
    (handle-result res)))

(defn add-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-data (-> req :params :static_page)]
    (if (s/valid? static-page-spec/add-static-page static-page-data)
      (let [[_ res] (static-page/add-static-page! slug static-page-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-slug (-> req :params :static_page_slug)
        static-page-data (-> req :params :static_page)]
    (if (s/valid? static-page-spec/update-static-page static-page-data)
      (let [[_ res] (static-page/update-static-page! slug static-page-slug static-page-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-static-page [req]
  (let [slug (-> req :params :slug)
        static-page-slug (-> req :params :static_page_slug)
        [_ res] (static-page/delete-static-page! slug static-page-slug)]
    (handle-result res)))

(defn add-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-data (-> req :params :navbar_item)]
    (if (s/valid? navbar-item-spec/add-navbar-item navbar-item-data)
      (let [[_ res] (navbar-item/add-navbar-item! slug navbar-item-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn edit-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-id (-> req :params :navbar_item_id)
        navbar-item-data (-> req :params :navbar_item)]
    (if (s/valid? navbar-item-spec/update-navbar-item navbar-item-data)
      (let [[_ res] (navbar-item/update-navbar-item! slug navbar-item-id navbar-item-data)]
        (handle-result res))
      (handle-invalid-spec))))

(defn delete-navbar-item [req]
  (let [slug (-> req :params :slug)
        navbar-item-id (-> req :params :navbar_item_id)
        [_ res] (navbar-item/delete-navbar-item! slug navbar-item-id)]
    (handle-result res)))

(defn current-user [req]
  (let [auth-user (-> req :auth-user)]
    (handle 200 {:user auth-user})))

(defn update-user [req]
  (let [auth-user (-> req :auth-user)
        user (-> req :params :user)]
    (if (s/valid? user-spec/update-user user)
      (let [[_ res] (user/update-user! auth-user user)]
        (handle-result res))
      (handle-invalid-spec))))
