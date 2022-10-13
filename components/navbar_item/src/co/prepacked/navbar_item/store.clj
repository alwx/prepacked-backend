(ns co.prepacked.navbar-item.store
  (:require
   [clojure.java.jdbc :as jdbc]
   [honey.sql :as sql]
   [co.prepacked.database.interface-ns :as database]))

(defn navbar-items [city-id]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:= :city_id [:cast city-id :uuid]]}
        results (jdbc/query (database/db) (sql/format query))]
    results))

(defn find-by [con city-id key value]
  (let [query {:select [:*]
               :from   [:navbar_item]
               :where  [:and
                        [:= key value]
                        [:= :city_id [:cast city-id :uuid]]]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-id [con city-id id]
  (find-by con city-id :id [:cast id :uuid]))

(defn insert-navbar-item! [con input]
  (let [query {:insert-into [:navbar_item]
               :values [(-> input
                            (select-keys [:city_id :title :priority :link])
                            (update :city_id database/->uuid))]}
        result (jdbc/execute! con (sql/format query) {:return-keys ["id"]})]
    (:id result)))

(defn update-navbar-item! [con id input]
  (let [query {:update :navbar_item
               :set    (-> input
                           (select-keys [:title :priority :link])
                           (update :city_id database/->uuid))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))

(defn delete-navbar-item! [con id]
  (let [query {:delete-from :navbar_item
               :where [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))
