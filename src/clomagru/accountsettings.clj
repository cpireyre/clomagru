(ns clomagru.accountsettings
  (:require [hiccup.form :as form]
            [next.jdbc.sql :as sql]
            [ring.util.response :refer [response status]]
            [clomagru.db :as db]
            [clomagru.initdb :refer [ds]]
            [clomagru.log :refer [timelog-stdin]]))

(defn update-user! [user-uuid params]
  (let [user-info (sql/query
                    ds ["select * from accounts where id = ?" user-uuid])]
    (str user-info)))

(defn handler [{params :params}]
  (if-let [user-uuid (db/match-credentials params)]
    (update-user! user-uuid params)
    (-> (response "You need to be logged in to update your account!")
        (status 401))))
