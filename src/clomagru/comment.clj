(ns clomagru.comment
  (:require [next.jdbc.sql :as sql]
            [clomagru.initdb :refer [ds]]
            [clomagru.db :as db]
            [clomagru.gallery :refer [string-uuid?]]
            [clomagru.users :refer [valid-username?]]))

(defn good-comment? [poster-uuid pic-uuid comment]
  "Checks that UUIDs were passed, and that these UUIDs refer
  to actual entries in the corresponding tables. Also forbids
  comments which are too long."
  (and (string-uuid? poster-uuid)
       (db/get-username-by-uuid poster-uuid)
       (string-uuid? pic-uuid)
       (db/get-image pic-uuid)
       (< (count comment) 140)))

;; how to sanitize comment text?
(defn save-comment! [poster-uuid pic-uuid comment] 
  (when (good-comment? poster-uuid pic-uuid comment)
    (sql/insert! ds :comments {:poster_uuid poster-uuid
                               :pic_uuid    pic-uuid
                               :comment     comment})))
