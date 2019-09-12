(ns clomagru.initdb
  (:require [clomagru.log :as log]
            [next.jdbc :as jdbc]))

;;  "src/clomagru/db" directory needs to exist.
(defonce db {:dbtype "sqlite" :dbname "src/clomagru/db/database.db"})

;;   There is a decision point here. I'm choosing not to use an
;;  autoincrement primary key. Instead, I will provide UUIDs at
;;  the application layer.
;;   I did some quick research and I wasn't quite convinced by
;;  the arguments in favor of autoincrementation.
;;   I also just think UUIDs are more fun and interesting.

;;   However, SQLite support for this is bad. Namely, its integer
;;  type only goes up to 8 bytes where UUIDs need 16.
;;  I guess I'll just use strings.

(defn create-accounts-table! [datasource]
  (log/timelog-stdin "Creating accounts table in database.")
  (jdbc/execute! datasource ["CREATE TABLE accounts (
                              id TEXT PRIMARY KEY UNIQUE NOT NULL,
                              username TEXT UNIQUE NOT NULL,
                              email TEXT UNIQUE NOT NULL,
                              password TEXT NOT NULL,
                              created_at INTEGER NOT NULL,
                              confirmed INTEGER DEFAULT 0 ) "]))

(defn create-files-table! [datasource]
  (log/timelog-stdin "Creating files table in database.")
  (jdbc/execute! datasource ["CREATE TABLE files (
                              id TEXT PRIMARY KEY UNIQUE NOT NULL,
                              owner TEXT NOT NULL,
                              type TEXT NOT NULL,
                              data BLOB,
                              likes INTEGER,
                              created_at INTEGER NOT NULL )"]))

;;  The token thing is made of very unsophisticated raw UUIDs,
;;  because I don't really feel like learning a bunch of libraries
;;  to implement a proper JSON Web Token solution at this time.

(defn create-tokens-table! [datasource]
  (log/timelog-stdin "Creating tokens table in database.")
  (jdbc/execute! datasource ["CREATE TABLE tokens (
                             id INTEGER PRIMARY KEY,
                             token TEXT UNIQUE NOT NULL,
                             owner TEXT UNIQUE NOT NULL,
                             created_at INTEGER NOT NULL )"]))

;;  I'm not a fan of this. But seems best under SQL design constraints?
(defn create-likes-table! [datasource]
  (log/timelog-stdin "Creating likes table in database.")
  (jdbc/execute! datasource ["CREATE TABLE likes (
                              id INTEGER PRIMARY KEY,
                              liker_uuid TEXT NOT NULL,
                              pic_uuid TEXT NOT NULL ) "]))

(defn create-comments-table! [datasource]
  (log/timelog-stdin "Creating comments table in database.")
  (jdbc/execute! datasource ["CREATE TABLE comments (
                             id INTEGER PRIMARY KEY,
                             comment TEXT NOT NULL,
                             poster_uuid TEXT NOT NULL,
                             created_at INTEGER NOT NULL,
                             pic_uuid TEXT NOT NULL ) "]))

(defn init-datasource! [database]
  (log/timelog-stdin "Initializing" database)
  (if (not (.exists (clojure.java.io/as-file (:dbname database))))
    (let [datasource (jdbc/get-datasource database)]
      (create-files-table! datasource)
      (create-tokens-table! datasource)
      (create-accounts-table! datasource)
      (create-comments-table! datasource)
      (create-likes-table! datasource)
      datasource)
    (jdbc/get-datasource database)))

(defonce ds (init-datasource! db))
