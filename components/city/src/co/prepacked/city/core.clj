(ns co.prepacked.city.core
  (:require 
    [co.prepacked.category.interface-ns :as category]
    [co.prepacked.navbar-item.interface-ns :as navbar-item]
    [co.prepacked.static-page.interface-ns :as static-page]
    [co.prepacked.city.store :as store]))

(defn- add-city-dependencies [{:keys [id] :as city}]
  (let [[_ categories] (category/city-categories id)
        [_ static-pages] (static-page/city-static-pages id)
        [_ navbar-items] (navbar-item/city-navbar-items id)]
    (assoc city
      :categories categories
      :static_pages static-pages
      :navbar_items navbar-items)))

(defn cities []
  (let [cities (store/all-cities)]
    [true cities]))

(defn city-with-all-dependencies [slug]
  (if-let [city (store/find-by-slug slug)]
    [true {:city (add-city-dependencies city)}]
    [false {:errors {:slug ["Cannot find the city."]}}]))
