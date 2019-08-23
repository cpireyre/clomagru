(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [clomagru.page :as p]
            [clomagru.users :as users]
            [hiccup.page :refer [html5]]
            [hiccup.core :as hiccup]
            [ring.util.http-response :refer :all])
  (:import java.io.ByteArrayInputStream))

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
      (str "<h1>404</h1>"))
    (str "<h1>404!</h1>")))

(defn one-image [url]
  [:li [:p [:a {:href url}
            [:img {:src url :width "250px" :height "250px"}]]]])

(defn get-user-pics [user-uuid]
  (map (comp one-image #(str "/pics/" %) :files/id)
       (db/get-images-id-by-owner user-uuid)))

(defn user-gallery [uuid req]
  (let [username (db/get-username-by-uuid uuid)]
    (html5 (p/header username)
           (p/nav-bar req)
           [:main#gallery
            [:h1 username "'s gallery"]
            [:ul (get-user-pics uuid)]]
           p/footer)))

(defn get-user-gallery [req]
  (let [route-name (get-in req [:params :user])]
    (if (users/valid-username? route-name)
      (if-let [user-uuid (db/get-uuid-by-username route-name)]
        (user-gallery user-uuid req)
        (str "looks like " route-name " ain't home"))
      (str "no one's called that round here"))))
