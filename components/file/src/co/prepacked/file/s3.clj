(ns co.prepacked.file.s3
  (:require [amazonica.aws.s3 :as s3]
            [co.prepacked.env.interface-ns :as env]
            [co.prepacked.log.interface-ns :as log]))

(defn- s3-config []
  (or (env/get-var :s3)
      (do
        (log/warn "`:s3` needs to be added to `env.edn`!")
        (System/exit 1))))

(defn- s3-credentials [config]
  {:access-key (:access-key-id config)
   :secret-key (:secret-access-key config)
   :region (:region config)
   :endpoint (:endpoint config)
   :client-config {:path-style-access-enabled true}})

(defn- bucket-exists? [cred bucket-name]
  (->> (s3/list-buckets cred)
       (map :name)
       (some #{bucket-name})))

(defn- create-bucket [cred bucket-name]
  (s3/create-bucket cred bucket-name))

(defn put [bytes filename]
  (let [{:keys [bucket] :as config} (s3-config)
        cred (s3-credentials config)
        stream (java.io.ByteArrayInputStream. bytes)]
    (when-not (bucket-exists? cred bucket)
      (create-bucket cred bucket))
    (s3/put-object
     cred
     :canned-acl :public-read
     :bucket-name bucket
     :input-stream stream
     :key filename)))

(defn delete [filename]
  (let [{:keys [bucket] :as config} (s3-config)
        cred (s3-credentials config)]
    (s3/delete-object cred :bucket-name bucket :key filename)))

(defn s3-public-server-url []
  (:public-server-url (s3-config)))
