(ns clomagru.login
  (:require [clomagru.db :as db]
            [clomagru.users :as users]
            [clomagru.log :as log]
            [clomagru.token :refer [token-confirmed?]]
            [ring.util.response :refer [redirect]]))

;;  TODO:
;; This is probably busted if already logged in but
;; manually sending a POST request.
(defn handler [{session :session params :params}]
  (let [user-uuid (db/match-credentials params)
        new-session (assoc session :uuid user-uuid)]
    (if user-uuid
      (if (token-confirmed? user-uuid)
        (do
          (log/timelog-stdin user-uuid "logged in.")
          (-> (redirect "/")
              (assoc :session new-session)))
        (str "Please confirm your e-mail address.")) ;; TODO: Use flash here.
      (str "Could not find this account or password didn't match.")))) ;; TODO: Use flash here.

;; TODO: clean up this route to make it more RESTful depending
;; on user-agent.
(defn make-account-handler [req]
  (db/make-account (:params req))
  (redirect "/login"))

(defn wipe-session []
  (-> (redirect "/")
      (assoc :session nil)))

(defn logged-in? [session]
  (:uuid session))
