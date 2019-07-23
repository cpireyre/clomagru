(ns clomagru.db
  (:require [next.jdbc :as jdbc]))

(def db {:dbtype "sqlite" :dbname "database.db"})

(def ds (jdbc/get-datasource db))

(jdbc/execute! ds ["
                   CREATE TABLE accounts (
                                          id INT AUTOINCREMENT PRIMARY KEY,
                                          name TEXT UNIQUE NOT NULL,
                                          email TEXT UNIQUE NOT NULL) "])
