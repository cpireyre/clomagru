(ns clomagru.accountsettings
  (:require [hiccup.form :as form]
            [next.jdbc.sql :as sql]
            [crypto.password.pbkdf2 :as password]
            [ring.util.response :refer [response status content-type]]
            [clomagru.db :as db]
            [clomagru.users :as users]
            [clomagru.initdb :refer [ds]]
            [clomagru.gallery :refer [transit-str]]
            [clomagru.log :refer [timelog-stdin]]))

(defn update-user! [user-uuid params]
  (let [old-username (:username params)
        new-username (or (:new-username params) old-username)
        old-email    (db/get-email-by-uuid user-uuid)
        new-email    (or (:new-email params) old-email)
        old-password (:password params)
        new-password (or (:new-password params) old-password)
        hash         (password/encrypt new-password)]
    (sql/update!  ds :accounts
                 {:username new-username
                  :email    new-email
                  :password hash}
                 {:id user-uuid})
    {:username new-username :email new-email}))

(defn name-or-email-in-db? [{username :new-username email :new-email}]
  (seq (sql/query ds ["select id from accounts
                         where email = ? or username = ?"
                         email username]))) 

(def request-format-description
  (str "Request format: requires username, password, "
       "and one or more of new-username, new-email"
       ", new-password" \))

(defn make-response [data header] ;; TODO: redirect if user agent is a browser
  (if-let [accept-type (get header "accept")]
    (cond (= accept-type "text/plain")
          (-> data
              (str)
              (response)
              (content-type "text/plain"))
          (= accept-type "application/json")
          (-> data
              (transit-str)
              (response)
              (content-type "application/json")))))

(defn handler [{params :params headers :headers}]
  (if (and (users/valid-patch-request? params)
           (not (name-or-email-in-db? params)))
    (if-let [user-uuid (db/match-credentials params)]
      (make-response (update-user! user-uuid params) headers)
      (-> (response "Credentials did not match.")
          (content-type "text/plain")
          (status 401)))
    (-> (response request-format-description)
        (content-type "text/plain")
        (status 400))))
