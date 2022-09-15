(ns co.prepacked.log.interface-ns
  (:require [clojure.realworld.log.config :as config]
            [clojure.realworld.log.core :as core]))

(defn init []
  (config/init))

(defmacro info [& args]
  `(core/info ~args))

(defmacro warn [& args]
  `(core/info ~args))

(defmacro error [& args]
  `(core/error ~args))
