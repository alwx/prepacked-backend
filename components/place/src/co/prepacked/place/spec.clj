(ns co.prepacked.place.spec
  (:require
   [reitit.ring.middleware.multipart :as multipart]
   [spec-tools.data-spec :as ds]
   [co.prepacked.spec.interface-ns :as spec]))

(def fetch-osm-data
  (ds/spec {:name :core/fetch-osm-data
            :spec {:address spec/non-empty-string?}}))

(def add-place
  (ds/spec {:name :core/add-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?
                   :osm_place_id int?
                   :osm_lat float?
                   :osm_lon float?
                   :osm_amenity string?
                   :osm_city string?
                   :osm_city_district string?
                   :osm_country_code string?
                   :osm_house_number string?
                   :osm_postcode string?
                   :osm_road string?
                   :osm_suburb string?
                   :osm_display_name string?}}))

(def update-place
  (ds/spec {:name :core/update-place
            :spec {:address spec/non-empty-string?
                   :title spec/non-empty-string?
                   :description spec/non-empty-string?
                   :priority int?
                   :osm_place_id int?
                   :osm_lat float?
                   :osm_lon float?
                   :osm_amenity string?
                   :osm_city string?
                   :osm_city_district string?
                   :osm_country_code string?
                   :osm_house_number string?
                   :osm_postcode string?
                   :osm_road string?
                   :osm_suburb string?
                   :osm_display_name string?}}))

;; operations with `place-features`

(def add-feature-to-place
  (ds/spec {:name :core/add-feature-to-place
            :spec {:feature_id spec/slug?
                   :value string?
                   :priority int?}}))

(def update-feature-in-place
  (ds/spec {:name :core/update-feature-in-place
            :spec {:feature_id spec/slug?
                   :value string?
                   :priority int?}}))

;; operations with `place-files`

(def upload-file-for-place
  (ds/spec {:name :core/upload-file-for-place
            :spec {:priority int?
                   :copyright string?
                   :file multipart/temp-file-part}}))

(def update-file-in-place
  (ds/spec {:name :core/update-file-in-place
            :spec {:priority int?}}))