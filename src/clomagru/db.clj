(ns clomagru.db
  (:require [next.jdbc.sql :as sql]
            [clojure.pprint :refer [pprint]]
            [crypto.password.pbkdf2 :as password]
            [clomagru.log :as log]
            [clomagru.token :as token]
            [clomagru.users :as users :only [valid-user?]]
            [clomagru.initdb :refer [ds]]))

(defn get-uuid-by-username [name]
  (get-in (sql/find-by-keys ds :accounts {:username name})
          [0 :accounts/id]))

(defn get-username-by-uuid [id]
  (get-in (sql/find-by-keys ds :accounts {:id id})
          [0 :accounts/username]))

(defn get-images-id-by-owner [owner-id]
    (sort-by
      :files/created_at
      #(compare %2 %1)
      (sql/find-by-keys ds :files {:owner owner-id})))

(defn select-all-accounts []
  (sql/query ds ["select * from accounts"]))

(defn pretty-print-accounts []
  (str "<pre>"
       (with-out-str (pprint (select-all-accounts)))
       "</pre>"))

(defn destructure-form-input [form-params]
  {:username  (get form-params "username")
   :email     (get form-params "email")
   :password  (get form-params "password")})

(defn alnum? [s]
  (boolean (re-matches #"^[\w]+$" s)))

;;    This is another decision point.
;;  Wouldn't it be better to encrypt the password earlier in the process?
;;  Like, in the browser maybe, before it even gets sent via POST.
;;    There's also the question of which encryption to use.
;;  Quick research suggests Argon2 but this crypto.password library seems OK.

(defn create-account [{:keys [username email password]}]
  (let [id   (java.util.UUID/randomUUID)
        token   (java.util.UUID/randomUUID)
        hash (password/encrypt password)
        now  (System/currentTimeMillis)]
    (log/timelog-stdin "Creating account:" id username email hash)
    (token/save-token! ds id token)
    (token/send-confirm-email! email username token)
    (sql/insert! ds :accounts {:id         id
                               :username   username
                               :email      email
                               :password   hash
                               :created_at now})))

(defn unique-user? [credentials]  ;; Perhaps this should be one request with OR
  (and 
    (empty? (sql/find-by-keys ds :accounts {:username (:username credentials)}))
    (empty? (sql/find-by-keys ds :accounts {:email (:email credentials)}))))

;;   Re: logging of errors and unmet specs, it's probably best to
;;  decouple some of this validation logic and only log the stuff that could
;;  be flagged as malicious, instead of like, every single typo that comes
;;  through.

(defn make-account [user-info]
  (let [credentials (destructure-form-input user-info)]
    (when (and (users/valid-user? credentials) (unique-user? credentials))
      (create-account credentials))))

(defn save-file! [{:keys [owner-uuid data type]}]
  (let [id         (java.util.UUID/randomUUID)
        now        (System/currentTimeMillis)]
    (log/timelog-stdin "Saving" type "from" owner-uuid "as" id)
    (sql/insert! ds :files {:id         id
                            :owner      owner-uuid
                            :type       type
                            :data       data
                            :created_at now})))

(defn get-image [uuid]
  (sql/get-by-id ds :files uuid))

(defn get-pw-by-username [name]
  (get-in (sql/find-by-keys ds :accounts {:username name})
          [0 :accounts/password]))

(defn match-credentials [credentials]
  "Returns the user UUID if it exists and password checks out, nil otherwise."
  (let [password (:password credentials)
        username (:username credentials)]
    (when-let [hash-in-db (get-pw-by-username username)]
      (when (password/check password hash-in-db)
        (get-uuid-by-username username)))))

(defn ten-latest-pics []
  (sql/query ds ["select * from files order by created_at desc limit 10"]))

(defn five-pics-from-offset [offset]
  (sql/query ds [(str "select id, owner, created_at from files order by created_at desc limit 5 offset "
                      offset)]))
