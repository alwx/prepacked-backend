(ns co.prepacked.file.core
  (:require [java-time]
            [clojure.java.jdbc :as jdbc]
            [co.prepacked.database.interface-ns :as database]
            [co.prepacked.file.images :as images]
            [co.prepacked.file.s3 :as s3]
            [co.prepacked.file.store :as store]))

(defn file-by-id [con id]
  (store/find-by-id con id))

(defn- add-file! [con auth-user file-data]
  (let [now (java-time/instant)
        file-data' (merge file-data
                          {:user_id (:id auth-user)
                           :created_at now})
        file-id (store/insert-file! con file-data')]
    (if-let [file (store/find-by-id con file-id)]
      [true file]
      [false {:errors {:other ["Cannot insert the file into the database."]} :-code 500}])))

(defn handle-file-upload! [con auth-user file-input db-extras]
  (let [{:keys [content-type tempfile]} file-input
        ext (images/content-type->supported-ext content-type)
        uuid (.toString (java.util.UUID/randomUUID))
        filename (format "%s.%s" uuid ext)]
    (try
      (-> (images/resize-image tempfile ext 1200)
          (s3/put (format "images/%s" filename)))
      (-> (images/resize-image tempfile ext 400)
          (s3/put (format "thumbnail_images/%s" filename)))
      (add-file! con auth-user (merge {:server_url (s3/s3-public-server-url)
                                       :link filename}
                                      db-extras))
      (catch Exception e
        [false {:errors {:file (.toString e)} :-code 500}]))))

(defn update-file! [file-id input]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [old-file-data (store/find-by-id con file-id)]
      (let [input' (merge old-file-data input)]
        (store/update-file! con file-id input')
        (if-let [file (store/find-by-id con file-id)]
          [true file]
          [false {:errors {:other ["Cannot update the file in the database."]} :-code 500}]))
      [false {:errors {:place ["A file with the provided id doesn't exist."]} :-code 404}])))

(defn delete-file! [file-id]
  (jdbc/with-db-transaction [con (database/db)]
    (if-let [{:keys [link]} (store/find-by-id con file-id)]
      (try
        (s3/delete (format "images/%s" link))
        (s3/delete (format "thumbnail_images/%s" link))
        (store/delete-file! con file-id)
        [true nil]
        (catch Exception e
          [false {:errors {:file (.toString e)} :-code 500}]))
      [false {:errors {:file ["There is no file with the specified ID."]} :-code 404}])))