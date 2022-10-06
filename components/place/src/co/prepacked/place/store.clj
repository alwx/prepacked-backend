(ns co.prepacked.place.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [co.prepacked.database.interface-ns :as database]
   [honey.sql :as sql]))

(defn places [city-id places-list-id]
  (let [query {:select [:p.* :plp.comment]
               :from [[:place :p]]
               :join [[:places_list_place :plp] [:= :p.id :plp.place_id]
                      [:places_list :pl] [:= :plp.places_list_id :pl.id]]
               :where [:and
                       [:= :pl.city_id city-id]
                       [:= :pl.id places-list-id]]
               :order-by [[:p.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query) {:identifiers identity})]
    results))

(defn find-by [key value]
  (let [query {:select [:*]
               :from [:place]
               :where [:= key value]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn find-by-id [id]
  (find-by :id id))

(defn insert-place! [place-input]
  (let [result (jdbc/insert!
                (database/db)
                :place
                place-input
                {:return-keys ["id"]})]
    (-> result first first val)))

(defn update-place! [id place-input]
  (let [query {:update :place
               :set    place-input
               :where  [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-place! [id]
  (let [query {:delete-from :place
               :where [:= :id id]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn get-place-features [place-id]
  (let [query {:select [:pf.* :f.title :f.icon]
               :from [[:place_feature :pf]]
               :join [[:feature :f] [:= :f.id :pf.feature_id]]
               :where [:= :pf.place_id place-id]
               :order-by [[:f.priority :desc]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-place-feature [place-id feature-id]
  (let [query {:select [:*]
               :from [:place_feature]
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}
        results (jdbc/query (database/db) (sql/format query))]
    (first results)))

(defn insert-place-feature! [input]
  (jdbc/insert! (database/db) :place_feature input))

(defn update-place-feature! [place-id feature-id place-feature-input]
  (let [query {:update :place_feature
               :set place-feature-input
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))

(defn delete-place-feature! [place-id feature-id]
  (let [query {:delete-from :place_feature
               :where [:and
                       [:= :place_id place-id]
                       [:= :feature_id feature-id]]}]
    (jdbc/execute! (database/db) (sql/format query))))
