(ns co.prepacked.database.core
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [java-time]
            [ragtime.jdbc]
            [ragtime.core]
            [ragtime.reporter]
            [ragtime.strategy]
            [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log])
  (:import  [org.postgresql.util PGobject]))

(defmulti read-pgobject
  "Convert returned PGobject to Clojure value."
  #(keyword (when (some? %) (.getType ^PGobject %))))

(defmethod read-pgobject :jsonb
  [^PGobject x]
  (when-let [val (.getValue x)]
    (json/read-str val)))

(defmethod read-pgobject :default
  [^PGobject x]
  (.getValue x))

;; Extend clojure.java.jdbc's protocol for interpreting ResultSet column values.
(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [val _ _]
    (read-pgobject val)))

(defn db-data []
  (or (env/get-var :db)
      (do
        (log/warn "`:db` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn db []
  (let [{:keys [dbname host port user password]} (db-data)]
    {:dbtype "postgresql"
     :dbname dbname
     :host host
     :port port
     :user user
     :password password}))

(defn run-migrations [db]
  (let [datastore (ragtime.jdbc/sql-database db)
        migration-index (atom {})
        migrations (ragtime.jdbc/load-resources "database/migrations")
        index (swap! migration-index ragtime.core/into-index migrations)
        options {:strategy ragtime.strategy/raise-error
                 :reporter ragtime.reporter/print}]
    (ragtime.core/migrate-all datastore index migrations options)))

(defn instant->sql-timestamp [instant]
  (java.sql.Timestamp/from instant))

(defn ->uuid [varchar]
  [:cast varchar :uuid])

(defn add-now-timestamps [m ks]
  (let [now (instant->sql-timestamp (java-time/instant))]
    (reduce (fn [m k]
              (assoc m k now))
            m
            ks)))