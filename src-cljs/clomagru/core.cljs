(ns clomagru-cljs.core
  (:require [reagent.core :as r]))

(defn henlo []
  [:h1 "React component here"])

(r/render [henlo]
          (js/document.getElementById "app"))
