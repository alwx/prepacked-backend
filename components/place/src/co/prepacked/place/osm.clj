(ns co.prepacked.place.osm
  (:require
   [clojure.edn :as edn]
   [clj-http.client :as client]
   [camel-snake-kebab.core :refer [->kebab-case-keyword]]
   [co.prepacked.env.interface-ns :as env]
   [co.prepacked.log.interface-ns :as log]))

(defmethod client/coerce-response-body :json-kebab-keys [req resp]
  (client/coerce-json-body req resp (memoize ->kebab-case-keyword) false))

(defn parse-query-param [param]
  (if (string? param)
    (try
      (edn/read-string param)
      (catch Exception _
        param))
    param))

(defn- osm []
  (or (env/get-var :osm)
      (do
        (log/warn "`:osm` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn request-place-osm-data [place]
  (let [{:keys [base-url user-agent email]} (osm)
        url (str base-url "/search")
        params {:accept :json
                :headers {"User-Agent" user-agent}
                :query-params {"q" (:address place)
                               "format" "json"
                               "addressdetails" "1"
                               "email" email}
                :as :json-kebab-keys
                :throw-exceptions false}
        osm-object (-> (client/get url params)
                       :body
                       first)] 
    {:osm_place_id (-> osm-object :place-id)
     :osm_lat (-> osm-object :lat parse-query-param)
     :osm_lon (-> osm-object :lon parse-query-param)
     :osm_amenity (-> osm-object :address :amenity)
     :osm_city (-> osm-object :address :city)
     :osm_city_district (-> osm-object :address :city-district)
     :osm_country_code (-> osm-object :address :country-code)
     :osm_house_number (-> osm-object :address :house-number)
     :osm_postcode (-> osm-object :address :postcode) 
     :osm_road (-> osm-object :address :road)
     :osm_suburb (-> osm-object :address :suburb)
     :osm_display_name (-> osm-object :display-name)}))
