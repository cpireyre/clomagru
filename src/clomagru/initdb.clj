(ns clomagru.initdb
  (:require [clomagru.log :as log]
            [next.jdbc :as jdbc]))

;;   There is a decision point here. I'm choosing not to use an
;;  autoincrement primary key. Instead, I will provide UUIDs at
;;  the application layer.
;;   I did some quick research and I wasn't quite convinced by
;;  the arguments in favor of autoincrementation.
;;   I also just think UUIDs are more fun and interesting.

;;   However, SQLite support for this is bad. Namely, its integer
;;  type only goes up to 8 bytes where UUIDs need 16.
;;  I guess I'll just use strings.

(defn create-accounts-table [datasource]
  (log/timelog-stdin "Creating accounts table in database.")
  (jdbc/execute! datasource ["CREATE TABLE accounts (
                              id TEXT PRIMARY KEY UNIQUE NOT NULL,
                              name TEXT UNIQUE NOT NULL,
                              email TEXT UNIQUE NOT NULL,
                              password TEXT NOT NULL,
                              created_at INTEGER NOT NULL ) "]))

(defn create-files-table [datasource]
  (log/timelog-stdin "Creating files table in database.")
  (jdbc/execute! datasource ["CREATE TABLE files (
                              id TEXT PRIMARY KEY UNIQUE NOT NULL,
                              owner TEXT NOT NULL,
                              type TEXT NOT NULL,
                              data BLOB,
                              created_at INTEGER NOT NULL )"]))

(defn init-datasource [database]
  (log/timelog-stdin "Initializing" database)
  (if (not (.exists (clojure.java.io/as-file (:dbname database))))
    (let [datasource (jdbc/get-datasource database)]
      (create-accounts-table datasource)
      (create-files-table datasource)
      datasource)
    (jdbc/get-datasource database)))