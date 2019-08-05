(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [ring.util.http-response :refer :all])
  (:import java.io.ByteArrayInputStream))

(defn string-uuid? [s]
  (if (re-find
        #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
           s)
    true
    false))

;; TODO: Fix status codes
(defn get-image [uuid]
  (if (string-uuid? uuid)
    (if-let [picc (first (db/get-image uuid))]
      (-> (ByteArrayInputStream. (:files/data picc))
          (ok)
          (content-type (:files/type picc)))
      (str "<h1>404</h1>"))
    (str "<h1>404!</h1>")))
