(ns clomagru.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [clojure.pprint :refer [pprint]]
            [crypto.password.pbkdf2 :as password]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clomagru.log :as log]))

(def db {:dbtype "sqlite" :dbname "src/clomagru/db/database.db"})

(def ds (jdbc/get-datasource db))

;;   There is a decision point here. I'm choosing not to use an
;;  autoincrement primary key. Instead, I will provide UUIDs at
;;  the application layer.
;;   I did some quick research and I wasn't quite convinced by
;;  the arguments in favor of autoincrementation.
;;   I also just think UUIDs are more fun and interesting.

;;   However, SQLite support for this is bad. Namely, its integer
;;  type only goes up to 8 bytes where UUIDs need 16.
;;  I guess I'll just use strings.

; (jdbc/execute! ds ["
;                    CREATE TABLE accounts (
;                    id TEXT PRIMARY KEY UNIQUE NOT NULL,
;                    name TEXT UNIQUE NOT NULL,
;                    email TEXT UNIQUE NOT NULL,
;                    password TEXT NOT NULL,
;                    created_at INTEGER NOT NULL ) "])

;;    This is another decision point.
;;  Wouldn't it be better to encrypt the password earlier in the process?
;;  Like, in the browser maybe, before it even gets sent via POST.
;;    There's also the question of which encryption to use.
;;  Quick research suggests Argon2 but this crypto.password library seems OK.

;; Maybe pass db as argument to this fn?

(defn select-all-accounts []
  (jdbc/execute! ds ["select * from accounts"]))

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

(defn make-account [user-info]
  (let [credentials (destructure-form-input user-info)]
    (if (b/valid? credentials user-info-validations) 
      (create-account credentials)
      nil)))

; (jdbc/execute! ds ["
;                    CREATE TABLE files (
;                     id TEXT PRIMARY KEY UNIQUE NOT NULL,
;                     owner TEXT NOT NULL,
;                     type TEXT NOT NULL,
;                     data BLOB,
;                     created_at INTEGER NOT NULL )"])

;;  TODO:
;;  Why on Earth am I saving the owner username instead of UUID?
(defn save-file! [{:keys [owner data type]}]
  (let [id (java.util.UUID/randomUUID)]
    (log/timelog-stdin "Saving" type "from" owner "as" id)
    (sql/insert! ds :files {:id id
                            :owner owner
                            :type type
                            :data data
                            :created_at (System/currentTimeMillis)})))

(defn get-images-id-by-owner [owner]
  (jdbc/execute! ds [(str
                       "SELECT id FROM files "
                       "WHERE owner ='"
                       owner
                       "'")]))

(defn get-image [uuid]
  (jdbc/execute! ds [(str
                       "SELECT type, data FROM files "
                       "WHERE id ='"
                       uuid
                       "'")]))
