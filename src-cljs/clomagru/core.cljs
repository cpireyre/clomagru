(ns clomagru-cljs.core
  (:require [reagent.core :as r]))

(def meme-font (r/atom "sans-serif"))

(defn henlo []
  [:main
   [:style "*{ font-family:" @meme-font ";"]
   [:input {:type "button"
            :value "Clicc me!"
            :on-click #(swap! meme-font (constantly "comic sans ms"))}]])

(r/render [henlo]
          (js/document.getElementById "app"))
