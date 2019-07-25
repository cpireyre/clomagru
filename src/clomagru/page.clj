(ns clomagru.page
  (:require [hiccup.core :as hiccup]))

(def register-form
  [:form {:method "post" :action "make-account"}
   [:fieldset
    [:legend "Make an account"] [:br]
    [:label "Name: " [:input {:name "username" :value "guy garvey"}]] [:br]
    [:label "E-mail: " [:input {:name "email" :value "meme@meme.meme"}]] [:br]
    [:label "Password: " [:input {:name "password" :value "henlo"}]] [:br]
    [:button "Submit"]
    ]])

(defn register-page []
  (hiccup/html register-form))
