(ns co.prepacked.file.images
  (:require [[mikera.image.core :as image.core]
             [mikera.image.protocols :as image.protocols]]))

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
