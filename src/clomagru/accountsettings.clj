(ns clomagru.accountsettings
  (:require [hiccup.form :as form]
            [next.jdbc.sql :as sql]
            [crypto.password.pbkdf2 :as password]
            [ring.util.response :refer [response redirect status content-type]]
            [clomagru.db :as db]
            [clomagru.users :as users]
            [clomagru.initdb :refer [ds]]
            [clomagru.gallery :refer [transit-str]]
            [clomagru.log :refer [timelog-stdin]]))

(defn dissoc-empty
  "Strips a map of empty entries. Map must only contain
  things on which empty? doesn't throw exception I guess."
  [m]
  (apply dissoc m
         (for [[k v] m :when (empty? v)] k)))

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
    (timelog-stdin old-username old-email "became" new-username new-email)
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
          (= accept-type "application/transit")
          (-> data
              (transit-str)
              (response)
              (content-type "application/transit"))
          :else (redirect "/"))))

(defn handler [{params :params headers :headers}]
  (let [input (dissoc-empty params)]
    (if (and (users/valid-patch-request? input)
             (not (name-or-email-in-db? input)))
      (if-let [user-uuid (db/match-credentials input)]
        (make-response (update-user! user-uuid input) headers)
        (-> (response "Credentials did not match.")
            (content-type "text/plain")
            (status 401)))
      (-> (response (str request-format-description input))
          (content-type "text/plain")
          (status 400)))))

