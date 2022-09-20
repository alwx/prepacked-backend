(ns co.prepacked.city.core
  (:require 
    [co.prepacked.category.interface-ns :as category]
    [co.prepacked.city.store :as store]))

(defn- add-city-dependencies [{:keys [id] :as city}]
  (let [[_ categories] (category/city-categories id)]
    (assoc city
      :categories categories)))

(defn cities []
  (let [cities (store/all-cities)]
    [true cities]))

(defn city-with-all-dependencies [slug]
  (if-let [city (store/find-by-slug slug)]
    [true {:city (add-city-dependencies city)}]
    [false {:errors {:slug ["Cannot find the city."]}}]))
