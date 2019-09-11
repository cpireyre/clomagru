(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [clomagru.page :as p]
            [clomagru.users :as users]
            [cognitect.transit :as transit]
            [hiccup.page :refer [html5]]
            [hiccup.core :as hiccup]
            [ring.util.http-response :refer :all]
            [ring.util.response :refer [redirect]])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

;; this seems stupid
(defn string-uuid? [s]
  (if (re-find
        #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
        s)
    true
    false))

(defn get-image [uuid]
  (if (string-uuid? uuid)
    (if-let [picc (db/get-image uuid)]
      (let [picdata (:files/data picc)
            pictype (:files/type picc)]
        (-> (ByteArrayInputStream. picdata)
            (ok)
            (content-type pictype)))
      (redirect "/404"))
    (redirect "/404")))

(defn one-image [url]
  [:li [:p [:a {:href url}
            [:img.userpic {:src url :width "250px" :height "250px"}]]]])

(defn get-user-pics [user-uuid]
  (map (comp one-image #(str "/pics/" %) :files/id)
       (db/get-images-id-by-owner user-uuid)))

(defn user-gallery [uuid req]
  (let [username (db/get-username-by-uuid uuid)]
    (html5 (p/header username)
           (p/nav-bar req)
           [:main#gallery
            [:h1 [:em username] "'s gallery"]
            [:ul (get-user-pics uuid)]]
           p/footer)))

(defn get-user-gallery [req]
  (let [route-name (get-in req [:params :user])]
    (if (users/valid-username? route-name)
      (if-let [user-uuid (db/get-uuid-by-username route-name)]
        (user-gallery user-uuid req)
        (str "looks like " route-name " ain't home")) ;; TODO: Use flash here.
      (str "no one's called that round here")))) ;; TODO: Use flash here.

(defn transit-str [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))

(defn get-gallery-page [page]
  (let [num (max page 1)  ;; no negative offset shenanigans
        page-offset (* 5 (dec num)) ;; 5 pics per page, no page 0
        pics (db/five-pics-from-offset page-offset)]
    (-> (transit-str pics)
        (ok)
        (header "Access-Control-Allow-Credentials" "true")
        (content-type "text/plain"))))
