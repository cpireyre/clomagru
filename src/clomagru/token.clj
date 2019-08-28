(ns clomagru.token
  (:require [clomagru.email :as email]
            [clomagru.log :as log]
            [next.jdbc.sql :as sql]))

(defn save-token!
  "Writes the confirmation token with the correspond owner into datasource."
  [datasource owner token]
  (sql/insert! datasource :tokens {:token token
                                   :owner owner
                                   :created_at (System/currentTimeMillis)}))

(defn confirmation-email [username token]
  (str "\tHello, " username ".\n"
       "You may want to visit this address:\n"
       "http://localhost:3000/confirm/" token))

(defn send-confirm-email!
  "Sends an email to the user containing the account confirmation link."
  [email username token]
  (email/send-mail! email
                    "Clomagru: confirm your registration."
                    (confirmation-email username token)))
