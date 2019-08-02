(ns clomagru-cljs.core
  (:require [reagent.core :as r]))

(defn henlo []
  [:h1 "henlo from reagent"])

(r/render [henlo]
          (js/document.getElementById "app"))
