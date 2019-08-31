(ns clomagru.index
  (:require [clomagru.db :as db]
            [hiccup.form :as form]))

(defn img-tag [pic-uuid]
  [:img.userpic {:src (str "/pics/" pic-uuid)
                 :width "250px"
                 :height "250px"}])

(defn get-pic-map
  [pic]
  {:id    (:files/id pic)
   :owner (db/get-username-by-uuid (:files/owner pic))
   :date  (java.util.Date. (:files/created_at pic))})

(defn comment-form [pic-uuid]
  (form/form-to
    [:post "/comment-picc"]
    [:fieldset [:legend "Thoughts?"]
     (form/text-field "comment")
     [:br]
     (form/submit-button "Thank you for your comment.")
     (form/submit-button "We will evaluate it's contents and use it to enhance our content.")
     (form/submit-button "We value your input.")
     (form/submit-button "Thank you for being you.")
     (form/submit-button "You are the only you that you have.")]))

(defn comments [uuid]
   [:ul
    [:li "all this and more"]
    [:li "to forget"]
    [:li "french grils"]
    [:li "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj"]])


(defn display-pic [{:keys [id owner date]}]
  [:li {:class "index-pic"}
   [:div {:class "column"}
    [:p [:em owner]]
    (img-tag id)
    [:p [:small date]]]
   [:div {:class "comments"}
    (comments id)
    [:hr]
    (comment-form id)]])

(defn recent-pics []
  (map (comp display-pic get-pic-map) (db/ten-latest-pics)))
