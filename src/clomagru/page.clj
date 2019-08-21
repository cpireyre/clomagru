(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]))

(defn header [title]
  [:head
   [:title title]
   [:link {:rel "stylesheet" :href "/styles.css"}]])

(def register-form
  [:form {:method "post" :action "make-account"}
   [:fieldset
    [:legend "Make an account"] [:br]
    [:label "Name: "
     [:input {:name     "username"
              :value    "guygarvey"
              :required true}]]
    [:br]
    [:label "E-mail: "
     [:input {:name     "email"
              :type     "email"
              :value    "meme@meme.meme"
              :required true}]]
    [:br]
    [:label "Password: "
     [:input {:name     "password"
              :type     "password"
              :value    "password123"
              :required true}]]
    [:br]
    [:button "Submit"]]])

(def nav-bar
  [:nav
   [:ul
    [:li [:a {:href "/"}         "Index"]]
    [:li [:a {:href "/login"}    "Sign in"]]
    [:li [:a {:href "/camera"}   "Take a photo"]]
    [:li [:a {:href "/list"}     "See all users"]]
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
  [:p [:em (:accounts/username user)]
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

(def login-form
  (form/form-to [:post "/login"]
   [:fieldset
    [:legend "Please enter your credentials"]
    [:label "Account name: "
     (form/text-field {:required true} "username")] [:br]
    [:label "Password: "
     (form/password-field {:required true} "password")] [:br]
    (form/submit-button "Submit")]))

(defn login-page []
  (html5 (header "Sign in") nav-bar login-form footer))
