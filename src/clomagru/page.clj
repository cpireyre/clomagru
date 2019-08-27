(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.index :as index]))

(defn nav-bar [{session :session}] (let [uuid     (:uuid session) username (db/get-username-by-uuid uuid)] [:nav
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
              :pattern  "^[a-zA-Z0-9]+$"
              :title    "Letters and numbers only."
              :required true}]]
    [:br]
    [:label "E-mail: "
     [:input {:name     "email"
              :type     "email"
              :required true}]]
    [:br]
    [:label "Password: "
     [:input {:name     "password"
              :pattern  ".{7,}"
              :title    "Seven characters or more."
              :type     "password"
              :required true}]] 
    [:br] [:button "Submit"]]])

(defn register-component [req]
  (if (get-in req [:session :uuid])
    [:p "You already have an account!"]
    register-form))

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

(defn login-component [req]
  (if-let [username (db/get-username-by-uuid (get-in req [:session :uuid]))]
    [:p (str "You're looking pretty logged in to me, " username ".")]
    login-form))

(defn index-gallery []
   [:ul#latest (index/recent-pics)])

(defn accounts-list []
  (let [accounts (db/select-all-accounts)]
    [:section
     [:h1 "People on this site"]
     [:ol
      (for [user accounts]
        [:li (print-one-user user)])]]))

(defn camera [req]
  (if (get-in req [:session :uuid])
    [:section
     [:h1 "Look alive!"]
     [:div {:id "app"}]
     [:script {:type "text/javascript" :src "app.js"}]
     pic-upload-form]
    [:section
     [:p "You need to be logged in for that."]]))


(def not-found
  [:section
  [:h1 "404"]
  [:p "We can't find this."]
  [:img {:height "500px;"
         :width "500px;"
         :src "assets/04.png"}]])

;; TODO: Would be way cool to have a defroutes-style macro here.

(defn render-page [req title & xs]
  (html5
    (header title)
    (nav-bar req)
    [:main xs]
    footer))

(defn camera-page [req]
  (render-page req "Take a photo." (camera req)))
(defn list-accounts [req]
  (render-page req "They use Clomagru." (accounts-list)))

(defn login-page [req]
  (render-page req "Sign in." (login-component req) [:br]
               [:p [:img {:src "assets/03.png"
                      :width "500px;"
                      :height "500px;"}]]))

(defn register-page [req]
  (render-page req "Join Clomagru." (register-component req)
               [:br]
               [:p [:img {:src "assets/01.png"
                          :width "500px;"
                          :height "500px;"}]]))
(defn index-page [req]
  (render-page req "Welcome to Clomagru!" (index-gallery)))
(defn not-found-page [req]
  (render-page req "404 not found." not-found))
