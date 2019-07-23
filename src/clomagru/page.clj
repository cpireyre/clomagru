(ns clomagru.page
  (:require [hiccup.core :as hiccup]))

(def nav-bar
  [:nav
   [:ul
    [:li [:a "Home"]]
    [:li [:a "Memes"]]
    [:li [:a "& More"]]]])

(defn sq [num]
  (hiccup/html [:h1 (str "I squared " num)]
               [:p (str "It's " (* num num) ".")]))

(def pics
  [:ul
   [:li [:img {:src "assets/01.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/02.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/03.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/04.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/05.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/06.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/07.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/08.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/09.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/10.png" :style "max-width: 25%; max-height: 25%;"}]]
   [:li [:img {:src "assets/11.png" :style "max-width: 25%; max-height: 25%;"}]]])

(def illustrations (hiccup/html pics))

(def henlo
  (hiccup/html nav-bar))
