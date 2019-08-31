(ns clomagru.upload
  (:require [clomagru.db :as db]
            [ring.util.response :refer [content-type response redirect]])
  (:import [java.io ByteArrayOutputStream FileInputStream]))

(defn file->byte-array [x]
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

(defn save-image! [req]
  (if (get-in req [:session :uuid])
    (let [owner-uuid (get-in req [:session :uuid])
          file-info (get (:multipart-params req) "file")
          tempfile (:tempfile file-info)
          filename (:filename file-info)
          content-type (:content-type file-info)]
      (when (or (= content-type "image/png")
                (= content-type "image/jpeg"))
        (db/save-file! {:owner-uuid owner-uuid
                        :type       content-type
                        :data       (file->byte-array tempfile)}))
      (let [username (db/get-username-by-uuid owner-uuid)]
        (-> (redirect (str "/gallery/" username)))))
    (-> (redirect "/"))))
