(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]))

(defn header [title]
  [:head
   [:title title]
   [:link {:rel "stylesheet" :href "/styles.css"}]])

(def register-form
  [:form {:method "post" :action "make-account"}
   [:fieldset
    [:legend "Make an account"] [:br]
    [:label "Name: "
     [:input {:name "username" :value "guy garvey"}]] [:br]
    [:label "E-mail: "
     [:input {:name "email" :type "email" :value "meme@meme.meme"}]] [:br]
    [:label "Password: "
     [:input {:name "password" :type "password" :value "henlo"}]] [:br]
    [:button "Submit"]]])

(def nav-bar
  [:nav
   [:ul
    [:li [:a {:href "/"} "Index"]]
    [:li [:a {:href "/camera"} "Take a photo"]]
    [:li [:a {:href "/list"} "See all users"]]
    [:li [:a {:href "/register"} "Sign up"]]]
   [:hr]])

(def footer
  [:footer
   [:hr]
   [:p "Powered by Clojure or whatever."]])

(defn register-page []
  (html5 (header "Join Clomagru") nav-bar register-form footer))

(defn index-page []
  (html5 (header "Clomagru home page") nav-bar
               [:h1 "Clomagru"]
               [:h2 "Coming soon."]
               footer))

(defn print-one-user [user]
  [:p [:em (:accounts/name user)]
       " signed up on "
       [:time (str (java.util.Date. (:accounts/created_at user)))]
       "."])

(defn list-accounts [accounts]
  (html5
    (header "They use Clomagru")
    nav-bar
    [:h1 "People on this site"]
    [:ol
     (for [user accounts]
       [:li (print-one-user user)])]
    footer))

(def pic-upload-form
  [:form {:method "post" :action "upload-picc" :enctype "multipart/form-data"}
   [:fieldset
    [:legend "Choose an image"] 
    (form/file-upload {:accept "image/jpeg, image/png"} "file") [:br]
    (form/submit-button "Submit")]])

(def camera
  [:main
   [:h1 "Look alive!"]
   [:div {:id "app"}]
   [:script {:type "text/javascript" :src "app.js"}]])

(defn camera-page []
  (html5 (header "Take a photo") nav-bar camera pic-upload-form footer))
