(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [ring.util.http-response :refer :all])
;;  "files" below is the name of the database table...
;;  Database internals are leaking.

(defn get-image [uuid]
  (if (uuid? uuid)
    (if-let [picc (first (db/get-image uuid))]
      (clojure.pprint/pprint (:files/data picc))
      (not-found))
    (str "All pics are referred to by UUIDs around here.")))
