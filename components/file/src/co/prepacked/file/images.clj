(ns co.prepacked.file.images
  (:require [mikera.image.core :as image.core]
            [mikera.image.protocols :as image.protocols])
  (:import (java.nio.file Files)))

(defn content-type->supported-ext [content-ext]
  (get {"image/jpeg" "jpg"
        "image/png" "png"
        "image/webp" "webp"}
       content-ext))

(defn resize-image [^java.io.File file ext new-width]
  (let [resource (image.protocols/as-image (.getAbsolutePath file))
        baos (java.io.ByteArrayOutputStream.)]
    (-> resource
        (image.core/resize new-width)
        (image.core/write baos ext :quality 0.8))
    (.toByteArray baos)))

(defn process-image [^java.io.File file ext new-width]
  (if (= ext "webp")
    (Files/readAllBytes (.toPath file))
    (resize-image file ext new-width)))