(ns clomagru.comment
  (:require [next.jdbc.sql :as sql]
            [clomagru.initdb :refer [ds]]
            [clomagru.log :as log]
            [clomagru.db :as db]
            [clomagru.gallery :refer [string-uuid?]]
            [clomagru.users :refer [valid-username?]]
            [ring.util.response :as response]
            [clomagru.gallery :as gallery]))

(defn good-comment? [poster-uuid pic-uuid comment]
  "Checks that UUIDs were passed, and that these UUIDs refer
  to actual entries in the corresponding tables. Also forbids
  comments which are too long."
  (and (string-uuid? poster-uuid)
       (db/get-username-by-uuid poster-uuid)
       (string-uuid? pic-uuid)
       (db/get-image pic-uuid)
       (< (count comment) 141)))

;; how to sanitize comment text?
(defn save-comment! [poster-uuid pic-uuid comment] 
  (if (good-comment? poster-uuid pic-uuid comment)
    (do
      (log/timelog-stdin poster-uuid "commented on" pic-uuid)
      (let [comment-data {:poster_uuid poster-uuid
                          :created_at (System/currentTimeMillis)
                          :pic_uuid    pic-uuid
                          :comment     comment}]
        (sql/insert! ds :comments comment-data)
        (-> (gallery/transit-str comment-data)
            (response/response)
            (response/status 201)
            (response/content-type "text/plain"))))
    :bad-comment))

(defn post-comment! [req] ;; maybe just asyc this.
  (if-let [user-uuid (get-in req [:session :uuid])]
    (str (save-comment!
      (get-in req [:session :uuid])
      (get-in req [:params :pic-uuid])
      (get-in req [:params :comment]))) ;; this doesn't return a 201 and doesn't really redirect where I would like.
    (-> (response/response "Unauthorized")
        (response/status 401)
        (response/content-type "text/plain"))))
