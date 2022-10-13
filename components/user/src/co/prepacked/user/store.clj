(ns co.prepacked.user.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [honey.sql :as sql]))

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
  (find-by con :id id))

(defn insert-user! [con user-input]
  (jdbc/insert! con :app_user user-input))

(defn update-user! [con id user-input]
  (let [query {:update :app_user
               :set    user-input
               :where  [:= :id id]}]
    (jdbc/execute! con (sql/format query))))
