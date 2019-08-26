(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.index :as index]))

(defn header [title & rest]
  [:head
   [:title title rest]
   [:link {:rel "stylesheet" :href "/styles.css"}]])

(def register-form
  [:form {:method "post" :action "make-account"}
   [:fieldset
    [:legend "Make an account"] [:br]
    [:label "Name: "
     [:input {:name     "username"
              :required true}]]
    [:br]
    [:label "E-mail: "
     [:input {:name     "email"
              :type     "email"
              :required true}]]
    [:br]
    [:label "Password: "  ;; TODO: make length constraint explicit
     [:input {:name     "password"
              :type     "password"
              :required true}]]
    [:br]
    [:button "Submit"]]])

(defn nav-bar [{session :session}]
  (let [uuid     (:uuid session)
        username (db/get-username-by-uuid uuid)]
    [:nav
     [:ul
      [:li [:strong [:a#sitename {:href "/"} "Clomagru"]]]
      (when username
        [:li "Henlo, " [:a {:href (str "/gallery/" username)} username]])
      [:li [:a {:href "/camera"} "Take a photo"]]
      [:li [:a {:href "/list"}   "See all users"]]
      (if uuid
        [:li [:a {:href "/logout"}   "Log out"]]
      '([:li [:a {:href "/register"} "Sign up"]]
        [:li [:a {:href "/login"}    "Sign in"]]))]
     [:hr]]))

(def footer
  [:footer
   [:hr]
   [:p "Powered by Clojure or whatever."]])

(defn register-page [req]
  (html5 (header "Join Clomagru") (nav-bar req)
         [:main register-form]
         footer))

(def index-gallery
  [:main#latest
   [:ul
    (index/recent-pics)]])

(defn index-page [req]
  (html5 (header "Clomagru home page") (nav-bar req)
         index-gallery
         footer))

(defn print-one-user [user]
  [:p [:em
       [:a {:href (str "/gallery/" (:accounts/username user))}
        (:accounts/username user)]]
       " signed up on "
       [:time (str (java.util.Date. (:accounts/created_at user)))]
       "."])

(defn list-accounts [req]
  (let [accounts (db/select-all-accounts)]
    (html5
      (header "They use Clomagru")
      (nav-bar req)
      [:main
       [:h1 "People on this site"]
       [:ol
        (for [user accounts]
          [:li (print-one-user user)])]]
      footer)))

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
   [:script {:type "text/javascript" :src "app.js"}]
   pic-upload-form])

(defn camera-page [req]
  (html5 (header "Take a photo") (nav-bar req) camera footer))

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
  (html5
    (header "Sign in")
    (nav-bar req)
    [:main
     (if-let [username (db/get-username-by-uuid (get-in req [:session :uuid]))]
       [:p (str "You're looking pretty logged in to me, " username ".")]
       login-form)]
    footer))
