(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [ring.util.http-response :refer :all])
  (:import java.io.ByteArrayInputStream))

;;  "files" below is the name of the database table...
;;  Database internals are leaking.
(defn get-image [uuid]
  (if-let [{:keys [files/type files/data]} (first (db/get-image uuid))]
    (-> (ByteArrayInputStream. data)
        (ok)
        (content-type type))
    (str "didn't find it")))
