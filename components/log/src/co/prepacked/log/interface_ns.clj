(ns co.prepacked.log.interface-ns
  (:require [co.prepacked.log.config :as config]
            [co.prepacked.log.core :as core]))

(defn init []
  (config/init))

(defmacro info [& args]
  `(core/info ~args))

(defmacro warn [& args]
  `(core/info ~args))

(defmacro error [& args]
  `(core/error ~args))
