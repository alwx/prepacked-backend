(ns co.prepacked.database.core
  (:require
   [clojure.java.io :as io]
   [co.prepacked.env.interface-ns :as env]
   [co.prepacked.log.interface-ns :as log]))

(defn db-path []
  (or (env/get-var :database)
      (do
        (log/warn "`:database` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn db
  ([path]
   {:classname   "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname     path})
  ([]
   (db (db-path))))

(defn db-exists? []
  (let [db-file (io/file (db-path))]
    (.exists db-file)))
