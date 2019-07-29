(ns clomagru.page
  (:require [hiccup.core :as hiccup]))

(def register-form
  [:form {:method "post" :action "make-account"}
   [:fieldset
    [:legend "Make an account"] [:br]
    [:label "Name: " [:input {:name "username" :value "guy garvey"}]] [:br]
    [:label "E-mail: " [:input {:name "email" :value "meme@meme.meme"}]] [:br]
    [:label "Password: " [:input {:name "password" :value "henlo"}]] [:br]
    [:button "Submit"]]])

(defn register-page []
  (hiccup/html index register-form))

(def index
  [:nav
   [:ul
    [:li [:a {:href "list"} "See all users"]]
    [:li [:a {:href "register"} "Sign up"]]
    [:li [:a {:href "/"} "Index"]]]])

(defn index-page []
  (hiccup/html index))

(defn print-one-user [user]
  [:p [:em (:accounts/name user)]
       " signed up on "
       (str (java.util.Date. (:accounts/created_at user)))
       "."])

(defn list-accounts [accounts]
  (hiccup/html
    [:ol
     (for [user accounts]
       [:li (print-one-user user)])]))
