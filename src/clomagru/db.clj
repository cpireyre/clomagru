(ns clomagru.db
  (:require [next.jdbc.sql :as sql]
            [clojure.pprint :refer [pprint]]
            [crypto.password.pbkdf2 :as password]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clomagru.log :as log]
            [clomagru.initdb :refer [init-datasource]]))

(def db {:dbtype "sqlite" :dbname "src/clomagru/db/database.db"})
(def ds (init-datasource db))

(defn get-uuid-by-username [name]
  (get-in (sql/find-by-keys ds :accounts {:name name})
          [0 :accounts/id]))

(defn get-images-id-by-owner [name]
  (let [id (get-uuid-by-username name)]
    (sql/find-by-keys ds :files {:owner id})))

(defn select-all-accounts []
  (sql/query ds ["select * from accounts"]))

(defn pretty-print-accounts []
  (str "<pre>"
       (with-out-str (pprint (select-all-accounts)))
       "</pre>"))

(defn destructure-form-input [form-params]
  {:username (get form-params "username")
   :email (get form-params "email")
   :password (get form-params "password")})

(defn alnum? [s]
  (boolean (re-matches #"^[\w]+$" s)))

;;    This is another decision point.
;;  Wouldn't it be better to encrypt the password earlier in the process?
;;  Like, in the browser maybe, before it even gets sent via POST.
;;    There's also the question of which encryption to use.
;;  Quick research suggests Argon2 but this crypto.password library seems OK.

(defn create-account [{:keys [username email password]}]
  (let [id (java.util.UUID/randomUUID)]
    (log/timelog-stdin "Creating account for" username "as" id)
    (sql/insert! ds :accounts {:id id
                               :name username
                               :email email
                               :password (password/encrypt password)
                               :created_at (System/currentTimeMillis)})))

(def user-info-validations
  {:username [v/required
              [alnum? :message "Name must only contain alphanumerics."]]
   :email v/required
   :password v/required})

;;  TODO:
;;  Check uniqueness of relevant user info before inserting.
(defn make-account [user-info]
  (let [credentials (destructure-form-input user-info)]
    (if (b/valid? credentials user-info-validations) 
      (create-account credentials)
      (log/timelog-stdin "User info did not pass validation" user-info))))

(defn save-file! [{:keys [owner data type]}]
  (let [id (java.util.UUID/randomUUID)]
    (log/timelog-stdin "Saving" type "from" owner "as" id)
    (sql/insert! ds :files {:id id
                            :owner (get-uuid-by-username owner)
                            :type type
                            :data data
                            :created_at (System/currentTimeMillis)})))

(defn get-image [uuid]
  (sql/get-by-id ds :files uuid))
