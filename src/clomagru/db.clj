(ns clomagru.db
  (:require [next.jdbc :as jdbc]
            [clojure.pprint :refer [pprint]]
            [crypto.password.pbkdf2 :as password]))

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

;; (jdbc/execute! ds ["
;;                    CREATE TABLE accounts (
;;                    id TEXT PRIMARY KEY UNIQUE NOT NULL,
;;                    name TEXT UNIQUE NOT NULL,
;;                    email TEXT UNIQUE NOT NULL,
;;                    password TEXT NOT NULL,
;;                    created_at INTEGER NOT NULL ) "])

;;    This is another decision point.
;;  Wouldn't it be better to encrypt the password earlier in the process?
;;  Like, in the browser maybe, before it even gets sent via POST.
;;    There's also the question of which encryption to use. Quick research suggests
;;  Argon2 but this crypto.password library seems alright.

(defn create-account [{:keys [username email password]}] ;; Maybe pass db as argument to this fn?
  (jdbc/execute! ds [(str
                       "INSERT INTO "
                       "accounts(id,name,email,password,created_at) "
                       "VALUES('"
                       (java.util.UUID/randomUUID)
                       "','" username
                       "','" email
                       "','" (password/encrypt password)
                       "','" (System/currentTimeMillis) "')")]))

(defn pretty-print-accounts []
  (str "<pre>"
       (with-out-str (pprint (jdbc/execute! ds ["select * from accounts"])))
       "</pre>"))
