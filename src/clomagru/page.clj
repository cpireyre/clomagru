(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.index :as index]))

(defn nav-bar [{session :session}]
  (let [uuid     (:uuid session)
        username (db/get-username-by-uuid uuid)]
    [:nav
     [:ul
      [:li [:strong [:a#sitename {:href "/"} "Clomagru"]]]
      (when username
        [:p
         [:li "Henlo, " [:a {:href (str "/gallery/" username)} username "."]]
         [:li [:a {:href "/camera"} "Take a photo"]]])
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

(defn print-one-user [user]
  [:p [:em
       [:a {:href (str "/gallery/" (:accounts/username user))}
        (:accounts/username user)]]
       " signed up on "
       [:time (str (java.util.Date. (:accounts/created_at user)))]
       "."])

(def pic-upload-form
  [:form {:method "post" :action "upload-picc" :enctype "multipart/form-data"}
   [:fieldset
    [:legend "Choose an image"] 
    (form/file-upload {:accept "image/jpeg, image/png"} "file") [:br]
    (form/submit-button "Submit")]])

(def login-form
  (form/form-to [:post "/login"]
   [:fieldset
    [:legend "Please enter your credentials"]
    [:label "Account name: "
     (form/text-field {:required true} "username")] [:br]
    [:label "Password: "
     (form/password-field {:required true} "password")] [:br]
    (form/submit-button "Submit")]))

(defn render-page [req title & xs]
  (html5
    (header title)
    (nav-bar req)
    [:main xs]
    footer))

(defn login-component [req]
  (if-let [username (db/get-username-by-uuid (get-in req [:session :uuid]))]
    [:p (str "You're looking pretty logged in to me, " username ".")]
    login-form))

(def index-gallery
   [:ul#latest (index/recent-pics)])

(defn accounts-list []
  (let [accounts (db/select-all-accounts)]
    [:section
     [:h1 "People on this site"]
     [:ol
      (for [user accounts]
        [:li (print-one-user user)])]]))

(def camera
  [:section
   [:h1 "Look alive!"]
   [:div {:id "app"}]
   [:script {:type "text/javascript" :src "app.js"}]
   pic-upload-form])


;; TODO: Would be way cool to have a defroutes-style macro here.

(defn camera-page [req]
  (render-page req "Take a photo" camera))
(defn list-accounts [req]
  (render-page req "They use Clomagru." (accounts-list)))
(defn login-page [req]
  (render-page req "Sign in" (login-component req)))
(defn register-page [req]
  (render-page req "Join Clomagru" register-form))
(defn index-page [req]
  (render-page req "Welcome to Clomagru!" index-gallery))
