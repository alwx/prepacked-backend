(ns co.prepacked.navbar-item.store
  (:require 
    [clojure.java.jdbc :as jdbc]
    [co.prepacked.database.interface-ns :as database]
    [honey.sql :as sql]))