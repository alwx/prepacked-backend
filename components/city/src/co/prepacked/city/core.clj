(ns co.prepacked.city.core
  (:require
   [co.prepacked.places-list.interface-ns :as places-list]
   [co.prepacked.navbar-item.interface-ns :as navbar-item]
   [co.prepacked.static-page.interface-ns :as static-page]
   [co.prepacked.city.store :as store]))

(defn cities []
  [true (store/all-cities)])

(defn city-by-slug [slug]
  (store/find-by-slug slug))

(defn- add-city-dependencies [{:keys [id] :as city}]
  (let [[_ places-lists] (places-list/get-places-lists id)
        [_ static-pages] (static-page/get-static-pages id)
        [_ navbar-items] (navbar-item/get-navbar-items id)]
    (assoc city
           :places_lists places-lists
           :static_pages static-pages
           :navbar_items navbar-items)))

(defn city-with-all-dependencies [slug]
  (if-let [city (store/find-by-slug slug)]
    [true {:city (add-city-dependencies city)}]
    [false {:errors {:city ["Cannot find the city."]}}]))
