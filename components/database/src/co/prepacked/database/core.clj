(ns co.prepacked.database.core
  (:require
   [ragtime.jdbc]
   [ragtime.core]
   [ragtime.reporter]
   [ragtime.strategy]
   [co.prepacked.env.interface-ns :as env]
   [co.prepacked.log.interface-ns :as log]))

(defonce db-data
  (or (env/get-var :db)
      (do
        (log/warn "`:db` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn db []
  (let [{:keys [dbname host port user password]} db-data]
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
