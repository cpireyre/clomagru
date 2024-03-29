(ns clomagru.page
  (:require [hiccup.core :as hiccup]
            [hiccup.form :as form]
            [hiccup.page :as page :refer [include-js html5]]
            [clomagru.users :as users]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.index :as index]))

(defn nav-bar [{session :session}]
  (let [uuid (:uuid session)
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
        '([:li [:a {:href "/info"}     "Settings"]]
         [:li [:a {:href "/logout"}    "Log out"]])
        '([:li [:a {:href "/register"} "Sign up"]]
          [:li [:a {:href "/login"}    "Sign in"]]))]
     [:hr]]))

(def footer
  [:footer
   [:hr]
   [:p "Powered by Clojure or whatever. "
    [:a {:href "https://www.vim.org"}
     [:img {:style "all: initial;"
            :src "https://www.vim.org/images/just_vim_it.gif" }]]
    "Illustrations by "
    [:a {:href "https://absurd.design"} "absurd.design"]
    "."]])

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
  [:p [:strong
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
     (form/password-field  "password")] [:br]
    (form/submit-button "Submit")
    [:br]
    (form/submit-button {:id "forgot-button"
                         :formaction "/reset"} "Forgot password?")]))

(defn login-component [req]
  (if-let [username (db/get-username-by-uuid (get-in req [:session :uuid]))]
    [:p (str "You're looking pretty logged in to me, " username ".")]
    login-form))

(defn index-gallery []
  [:div
   [:h1 "Some recents pics."]
   [:div {:id "app"}]
   [:script {:type "text/javascript" :src "/cljs-out/dev-main.js"}]])
    
(defn accounts-list []
  (let [accounts (db/select-all-accounts)]
    [:section
     [:h1 "People on this site."]
     [:ol
      (for [user accounts]
        [:li (print-one-user user)])]]))

(defn camera [req]
  (if (get-in req [:session :uuid])
    [:section
     [:h1 "Look alive!"]
     [:div {:id "app"}]
     [:script {:type "text/javascript" :src "/cljs-out/dev-main-camera.js"}]
     pic-upload-form]
    [:section
     [:p "You need to be logged in for that."]]))

(defn change-info-form [uuid]
  (form/form-to
    [:patch "/info"]
    [:fieldset
     [:legend "Change your user information."]
     [:label "Current username: "
      (form/text-field
        {:name     "username"
         :pattern  "^[a-zA-Z0-9]+$"
         :title    "Letters and numbers only."
         :value    (db/get-username-by-uuid uuid)
         :required true}
        "username")] [:br]
     [:label "Current password: "
      (form/text-field 
        {:name     "password"
         :pattern  ".{7,}"
         :title    "Seven characters or more."
         :type     "password"
         :required true}
        "password")] [:hr]
     [:label "New username? " (form/text-field
                                {:name     "new-username"
                                 :pattern  "^[a-zA-Z0-9]+$"
                                 :title    "Letters and numbers only."}
                                "new-username")] [:br]
     [:label "New password? " (form/text-field
                                {:name     "new-password"
                                 :pattern  ".{7,}"
                                 :title    "Seven characters or more."
                                 :type     "password"}
                                "new-password")] [:br]
     [:label "New e-mail? " (form/text-field
                              {:name "new-email"
                               :type "email"}
                              "new-email")] [:br]
     (form/submit-button "Submit")]))

(defn settings-page [{session :session}]
  (if-let [uuid (:uuid session)]
    [:section
     [:h1 "Manage your account."]
     (change-info-form uuid)
     [:p [:img {:src "/assets/10.png"
                :width "500px"
                :height "500px"}]]]
    (str "need to be logged in for that")))
(def not-found
  [:section
  [:h1 "404"]
  [:p "We can't find this."]
  [:p [:a {:href "/"} "Go baq."]]
  [:img {:height "500px;"
         :width "500px;"
         :src "/assets/04.png"}]])

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
  (render-page req "Sign in."
               [:h1 "Log in to your account."]
               (login-component req) [:br]
               [:p [:img {:src "/assets/03.png"
                      :width "500px;"
                      :height "500px;"}]]))

(defn register-page [req]
  (render-page req "Join Clomagru."
               [:h1 "Create your account."]
               (register-component req)
               [:br]
               [:p [:img {:src "/assets/01.png"
                          :width "500px"
                          :height "500px"}]]))
(defn index-page [req]
  (render-page req "Welcome to Clomagru!" (index-gallery)))
(defn change-account-info [req]
  (render-page req "Account settings." (settings-page req)))
(defn not-found-page []
  (html5 (header "404 not found.") [:main not-found] footer))
