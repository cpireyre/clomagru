(ns clomagru.forgot
  (:require [clomagru.email :as email]
            [clomagru.initdb :refer [ds]]
            [clomagru.log :refer [timelog-stdin]]
            [clomagru.db :as db]
            [clomagru.users :refer [example-pw]]
            [ring.util.response :as response]
            [next.jdbc.sql :as sql]
            [crypto.password.pbkdf2 :as password]))

(defn forgot-password-mail [username token]
  (str "Henlo, " username ".\n"
       "If you've forgotten your Clomagru password, "
       "you may reset it here:\n"
       "http://localhost:3000/reset/" token
       "\nIf you don't know what this e-mail is about, just delete it."))

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

;; TODO: check error pre conditions, add handler.

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

(defn get-user-token [owner-id]
  (sql/find-by-keys
    ds
    :pwtokens
    {:owner owner-id}))

(defn prep-request
  "Takes a request map and returns the user map if they are
  in a position to change their password, otherwise nil.
  The account must exist and be confirmed, and there must not be any pending
  tokens for this user."
  [req]
  (let [name-req (get-in req [:params :username])]
    (when-let
      [account (first (sql/find-by-keys ds :accounts {:username name-req}))]
      (when (and (= 1 (get account :accounts/confirmed))
                 (empty? (get-user-token (:accounts/id account))))
        account))))

(defn handler!
  "Takes a request and initiates the password reset procedure, if applicable."
  [req]
  (if-let [account (prep-request req)]
    (do
      (forgot-pw! account)
      (-> (response/response "Sent token.")
          (response/status 202)))
    (-> (response/response "There was an error which I can't be bothered to specify at this time.")
        (response/status 401))))

(defn reset-pw-handler!
  "Takes a token UUID and resets the user's password, if possible."
  [tok]
  (if-let [token-map (get-token ds tok)]
    (let [pw (example-pw)
          hash (password/encrypt pw)]
      (confirm-token! ds token-map hash)
      (-> (response/response (str "Success! You may now log in using "
                                  "this password: " pw
                                  "\nMake sure to change it swiftly."))
          (response/content-type "text/plain")))
    (-> (response/response "Could not find this token.")
        (response/content-type "text/plain")
        (response/status 404))))
