(ns co.prepacked.user.store
  (:require [clojure.java.jdbc :as jdbc]
            [honey.sql :as sql]
            [co.prepacked.database.interface-ns :as database]))

(defn find-by [con key value]
  (let [query {:select [:*]
               :from   [:app_user]
               :where  [:= key value]}
        results (jdbc/query con (sql/format query))]
    (first results)))

(defn find-by-email [con email]
  (find-by con :email email))

(defn find-by-username [con username]
  (find-by con :username username))

(defn find-by-id [con id]
  (find-by con :id [:cast id :uuid]))

(defn insert-user! [con input]
  (let [query {:insert-into [:app_user]
               :values [(-> input
                            (select-keys [:email :username :password])
                            (database/add-now-timestamps [:created_at :updated_at]))]}]
    (jdbc/execute! con (sql/format query))))

(defn update-user! [con id input]
  (let [query {:update :app_user
               :set    (-> input
                           (select-keys [:email :username :password])
                           (database/add-now-timestamps [:updated_at]))
               :where  [:= :id [:cast id :uuid]]}]
    (jdbc/execute! con (sql/format query))))
