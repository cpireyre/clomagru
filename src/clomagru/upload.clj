(ns clomagru.upload
  (:require [clomagru.db :as db]
            [ring.util.response :refer [content-type response redirect]])
  (:import [java.io ByteArrayOutputStream FileInputStream]))

(defn file->byte-array [x]
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

;;  TODO:
;;  redirect post response with ring.response
(defn save-image! [owner-uuid {:keys [tempfile filename content-type]}]
  (db/save-file! {:owner-uuid owner-uuid
                  :type       content-type
                  :data       (file->byte-array tempfile)})
  (let [username (db/get-username-by-uuid owner-uuid)]
    (-> (redirect (str "/gallery/" username)))))

