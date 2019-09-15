(ns clomagru.forgot
  (:require [clomagru.email :as email]
            [clomagru.initdb :refer [ds]]
            [clomagru.log :refer [timelog-stdin]]
            [clomagru.db :as db]
            [clomagru.users :refer [example-pw]]
            [next.jdbc.sql :as sql]))

(defn forgot-password-mail [username token]
  (str "Henlo, " username ".\n"
       "If you've forgotten your Clomagru password, "
       "you may reset it here:\n"
       "TBD " token))

(defn send-forgot-email! [email username token]
  (email/send-mail! email
                    "Clomagru: forgot your password?"
                    (forgot-password-mail username token)))

(defn get-user-info 
  "Takes the username, queries the datasource, returns e-mail and uuid.
  Returns nil if no such user was found."
  [ds username] ;; TODO: it'd probably be better to use sql/find-by-key.
  (let [ret (sql/query ds
                       ["select id, email, username from accounts where username = ?"
                        username])]
    (when (seq ret)
      (first ret))))

(defn get-token
  "Takes a token string and queries the datasource for the corresponding map.
  Returns nil on failure to find token."
  [ds tok]
  (let [ret (sql/query ds
                       ["select * from pwtokens where token = ?"
                        tok])]
    (when (seq ret)
      (first ret))))

(defn make-token
  "Takes a user UUID and returns a token map
  suitable for insertion in database."
  [owner-id]
  {:token      (-> (java.util.UUID/randomUUID) (.toString))
   :owner      owner-id
   :created_at (System/currentTimeMillis)})

(defn insert-token! [datasource token]
  (timelog-stdin "inserting token" token)
  (sql/insert! datasource :pwtokens token))

(defn forgot-pw!
  "Takes a user map, generates a token, sends the e-mail containing
  the password reset link."
  [user]
  (let [id       (:accounts/id user)
        username (:accounts/username user)
        tok      (make-token id)
        tok-str  (:token tok)
        email    (:accounts/email user)]
    (insert-token! ds tok)
    (send-forgot-email! email username tok-str)))

;; TODO: check error pre conditions, verify token, reset password, add handler.

(defn confirm-token!
  "Takes a datasource and token map, finds the corresponding user,
  resets their password, deletes the token from the database."
  [ds tok encrypted-new-pw]
  (timelog-stdin "processing password reset token" tok)
  (sql/update!
    ds
    :accounts
    {:password encrypted-new-pw}
    {:id (:pwtokens/owner tok)})
  (sql/delete! ds :pwtokens {:id (:pwtokens/id tok)}))
