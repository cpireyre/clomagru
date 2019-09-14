(ns clomagru.comment
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [clomagru.initdb :refer [ds]]
            [clomagru.log :as log]
            [clomagru.db :as db]
            [clomagru.gallery :refer [string-uuid?]]
            [clomagru.users :refer [valid-username?]]
            [ring.util.response :as response]
            [clomagru.gallery :as gallery]))

(def unauthorized (-> (response/response "Unauthorized")
                      (response/status 401)
                      (response/content-type "text/plain")))

(defn good-comment? [poster-uuid pic-uuid comment]
  "Checks that UUIDs were passed, and that these UUIDs refer
  to actual entries in the corresponding tables. Also forbids
  comments which are too long."
  (and (string? comment)
       (string-uuid? poster-uuid)
       (db/get-username-by-uuid poster-uuid)
       (string-uuid? pic-uuid)
       (db/get-image pic-uuid)
       (< (count comment) 141)))

(defn can-like? [liker-uuid pic-uuid]
  (and (string-uuid? liker-uuid)
       (db/get-username-by-uuid liker-uuid)
       (string-uuid? pic-uuid)
       (empty? 
         (sql/query ds
                    ["select * from likes where liker_uuid = ? and pic_uuid = ?"
                     liker-uuid
                     pic-uuid]))
       (db/get-image pic-uuid)))

;; how to sanitize comment text?
(defn save-comment! [poster-uuid pic-uuid comment] 
  (if (good-comment? poster-uuid pic-uuid comment)
    (do
      (let [comment-data {:poster_uuid poster-uuid
                          :created_at (System/currentTimeMillis)
                          :pic_uuid    pic-uuid
                          :comment     comment}]
        (sql/insert! ds :comments comment-data)
        (log/timelog-stdin "Comment:" comment-data)
        (-> comment-data
            (assoc :poster-name (db/get-username-by-uuid poster-uuid))
            (gallery/transit-str)
            (response/response)
            (response/status 201)
            (response/content-type "text/plain"))))
    :bad-comment))

(defn post-comment! [req] ;; maybe just asyc this.
  (if-let [user-uuid (get-in req [:session :uuid])]
    (save-comment!
      (get-in req [:session :uuid])
      (get-in req [:params :pic-uuid])
      (get-in req [:params :comment])) ;; this doesn't return a 201 and doesn't really redirect where I would like.
    unauthorized))

(defn user-like! [ds liker-id pic-id]
  (sql/insert! ds :likes {:liker_uuid liker-id :pic_uuid pic-id})
  (jdbc/execute-one! ds ["UPDATE files SET likes = likes + 1 WHERE id = ?" pic-id]))

(defn like [r id]
  (if (not (nil? (get-in r [:session :uuid])))
    (let [liker (.toString (get-in r [:session :uuid]))
          pic-uuid (.toString id)]
      (if (can-like? liker pic-uuid)
        (do
          (log/timelog-stdin liker "liked" pic-uuid)
          (user-like! ds liker pic-uuid)
          (-> (response/response (str liker " liked " pic-uuid))
              (response/header "Location:" (str "/pics/" pic-uuid))
              (response/content-type "text/plain")))
        (-> (response/response "Dislike")
            (response/status 403))))
    unauthorized))
