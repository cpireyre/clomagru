(ns clomagru.gallery
  (:require [clomagru.db :as db]
            [clomagru.page :as p]
            [hiccup.core :as hiccup]
            [ring.util.http-response :refer :all])
  (:import java.io.ByteArrayInputStream))

(defn string-uuid? [s]
  (if (re-find
        #"^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"
           s)
    true
    false))

;; TODO: Fix status codes
(defn get-image [uuid]
  (if (string-uuid? uuid)
    (if-let [picc (first (db/get-image uuid))]
      (-> (ByteArrayInputStream. (:files/data picc))
          (ok)
          (content-type (:files/type picc)))
      (str "<h1>404</h1>"))
    (str "<h1>404!</h1>")))

(defn one-image [url]
  [:li [:p [:a {:href url}
        [:img {:src url
               :width "250px"
               :height "250px"}]]]])

(defn get-user-pics [user]
  (map (comp one-image #(str "/pics/" %) :files/id)
       (db/get-images-id-by-owner "guy garvey")))

(defn get-user-gallery [user]
  (let [pics (get-user-pics user)]
    (hiccup/html (p/header (str user "'s gallery"))
                 p/nav-bar
                 [:main {:id "gallery"}
                  [:h1 (str user)]
                  [:ul
                   pics]]
                 p/footer)))
