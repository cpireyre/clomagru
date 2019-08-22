(ns clomagru.login
  (:require [clomagru.db :as db]
            [clomagru.users :as users]
            [clomagru.log :as log]
            [ring.util.response :refer [content-type response redirect]]))

;;  TODO:
;; This is probably busted if already logged in but
;; manually sending a POST request.
(defn handler [{session :session params :params}]
  (let [user-uuid (db/match-credentials params)
        new-session (assoc session :uuid user-uuid)]
    (if user-uuid
      (do
        (log/timelog-stdin user-uuid "logged in.")
        (-> (redirect "/")
            (content-type "text/plain")
            (assoc :session new-session)))
      (str "Could not find this account or password didn't match."))))

(defn wipe-session []
  (-> (redirect "/")
      (assoc :session nil)))

(defn logged-in? [session]
  (:uuid session))
