(ns clomagru.index
  (:require [clomagru.db :as db]))

(defn img-tag [pic-uuid]
  [:img.userpic {:src (str "/pics/" pic-uuid)
         :width "250px"
         :height "250px"}])

(defn get-pic-map
  [pic]
  {:id    (:files/id pic)
   :owner (db/get-username-by-uuid (:files/owner pic))
   :date  (java.util.Date. (:files/created_at pic))})

(defn display-pic [{:keys [id owner date]}]
  [:li
   [:p [:em owner]]
   (img-tag id)
   [:p [:small date]]])

(defn recent-pics []
  (map (comp display-pic get-pic-map) (db/ten-latest-pics)))
