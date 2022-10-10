(ns co.prepacked.file.core
  (:require [amazonica.aws.s3 :as s3]
            [mikera.image.core :as image.core]
            [mikera.image.protocols :as image.protocols]
            [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log]
            [co.prepacked.file.store :as store])
  (:import (java.nio.file Files)))

(defn- s3-config []
  (or (env/get-var :s3)
      (do
        (log/warn "`:s3` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn- s3-credentials [config]
  {:access-key (:access-key-id config)
   :secret-key (:secret-access-key config)
   :region (:region config)
   :endpoint "http://localhost:9001"
   :client-config {:path-style-access-enabled true}})

(defn- bucket-exists? [cred bucket-name]
  (->> (s3/list-buckets cred)
       (map :name)
       (some #{bucket-name})))

(defn- create-bucket [cred bucket-name]
  (s3/create-bucket cred bucket-name))



(defn content-type->supported-ext [content-ext]
  (get {"image/jpeg" "jpg"
        "image/png" "png"}
       content-ext))

(defn resize-image [^java.io.File file ext new-width]
  (let [resource (image.protocols/as-image (.getAbsolutePath file))
        baos (java.io.ByteArrayOutputStream.)]
    (-> resource
        (image.core/resize new-width)
        (image.core/write baos ext))
    (.toByteArray baos)))

(defn save-to-s3 [bytes filename]
  (let [{:keys [bucket] :as config} (s3-config)
        cred (s3-credentials config)
        stream (java.io.ByteArrayInputStream. bytes)]
    #_(when-not (bucket-exists? cred bucket)
        (create-bucket cred bucket))
    (s3/put-object
     cred
     :bucket-name bucket
     :input-stream stream
     :key filename
     :access-control-list {:grant-permission ["AllUsers" "Read"]})))

(defn add-file! [file-data]
  (store/insert-file! file-data)
  (if-let [file (store/find-by-id (:id file-data))]
    [true file]
    [false {:errors {:other ["Cannot insert the file into the database."]}}]))