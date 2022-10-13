(ns co.prepacked.static-page.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))

(defn static-pages [city-id]
  (let [query {:select [:*]
               :from   [:static_page]
               :where  [:= :city_id [:cast city-id :uuid]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from   [:static_page]
               :where  [:and 
                        [:= key value]
                        [:= :city_id [:cast city-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-slug [con city-id slug]
  (find-by con city-id :slug slug))

(defn insert-static-page! [con input]
  (let [query {:insert-into [:static_page]
               :values [(-> input
                            (select-keys [:city_id :slug :title :content])
                            (update :city_id database/->uuid)
                            (database/add-now-timestamps [:created_at :updated_at]))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-static-page! [con id input]
  (let [query {:update :static_page
               :set    (-> input
                           (select-keys [:slug :title :content])
                           (database/add-now-timestamps [:updated_at]))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-static-page! [con id]
  (let [query {:delete-from :static_page
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))
