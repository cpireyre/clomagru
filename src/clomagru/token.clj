(ns clomagru.token
  (:require [clomagru.email :as email]
            [clomagru.log :as log]
            [clomagru.initdb :refer [ds]]
            [ring.util.response :refer [redirect]]
            [next.jdbc.sql :as sql]))

(defn token-confirmed?
  "Looks in database if the user with corresponding UUID
  has confirmed their account."
  [user-uuid]
  (not-empty
    (sql/query
      ds
      ["select * from accounts where id = ? and confirmed = 1"
       user-uuid])))

(defn save-token!
  "Writes the confirmation token with the correspond owner into datasource."
  [datasource owner token]
  (sql/insert! datasource :tokens {:token token
                                   :owner owner
                                   :created_at (System/currentTimeMillis)}))

(defn confirmation-email [username token]
  (str "Henlo, " username ".\n"
       "You may want to visit this address:\n"
       "\thttp://localhost:3000/confirm/" token
       "\nIf you don't know what this e-mail is about, just delete it."))

(defn send-confirm-email!
  "Sends an email to the user containing the account confirmation link."
  [email username token]
  (email/send-mail! email
                    "Clomagru: confirm your registration."
                    (confirmation-email username token)))


(defonce uuid-regex
  #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$")


(defn get-owner-by-token [token]
  (-> (sql/query ds ["select owner from tokens where token = ?" token])
      first
      :tokens/owner))

(defn delete-token! [token]
  "Returns true if the delete! request changed the database,
  false if no update happened for whatever reason."
  (let [result-map (sql/delete! ds :tokens {:token token})]
    (let [deleted? (= (:next.jdbc/update-count result-map) 1)]
      (if deleted?
        (log/timelog-stdin "Deleted token" token)
        (log/timelog-stdin "Couldn't delete token" token))
      deleted?)))

(defn confirm-in-db! [owner]
  (log/timelog-stdin "Confirming user" owner)
  (sql/update! ds :accounts {:confirmed 1} {:id owner}))

(defn confirm-account! [token-uuid]
  (let [token (.toString token-uuid)]
    (if-let [owner (get-owner-by-token token)]
      (do
        (log/timelog-stdin "Processing token" token)
        (confirm-in-db! owner)
        (delete-token! token)
        (redirect "/"))
      (redirect "/404"))))
