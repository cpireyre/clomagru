(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]
            [clomagru.db :as db]))

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

(defn nav-bar [{session :session}]
  (let [uuid (:uuid session)
        username (db/get-username-by-uuid uuid)]
    [:nav
     [:ul
      [:li [:a {:href "/"}       "Index"]]
      [:li [:a {:href "/camera"} "Take a photo"]]
      [:li [:a {:href "/list"}   "See all users"]]
      (if uuid
        [:li (str "Hello, " username ".")]
      '([:li [:a {:href "/register"} "Sign up"]]
        [:li [:a {:href "/login"}    "Sign in"]]))]
     [:hr]]))

(def footer
  [:footer
   [:hr]
   [:p "Powered by Clojure or whatever."]])

(defn register-page [req]
  (html5 (header "Join Clomagru") (nav-bar req) register-form footer))

(defn index-page [req]
  (html5 (header "Clomagru home page") (nav-bar req)
               [:h1 "Clomagru"]
               [:h2 "Coming soon."]
               footer))

(defn print-one-user [user]
  [:p [:em (:accounts/username user)]
       " signed up on "
       [:time (str (java.util.Date. (:accounts/created_at user)))]
       "."])

(defn list-accounts [req accounts]
  (html5
    (header "They use Clomagru")
    (nav-bar req)
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

(defn camera-page [req]
  (html5 (header "Take a photo") (nav-bar req) camera pic-upload-form footer))

(def login-form
  (form/form-to [:post "/login"]
   [:fieldset
    [:legend "Please enter your credentials"]
    [:label "Account name: "
     (form/text-field {:required true} "username")] [:br]
    [:label "Password: "
     (form/password-field {:required true} "password")] [:br]
    (form/submit-button "Submit")]))

(defn login-page [req]
  (html5 (header "Sign in") (nav-bar req) login-form footer))
