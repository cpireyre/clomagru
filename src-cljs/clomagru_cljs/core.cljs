(ns clomagru-cljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clomagru.index :as index]
            [cognitect.transit :as t]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(defn page-url [pagenum]
  (str "http://localhost:3000/page/" pagenum))

(defn get-page! [page state]
  (go (let [r        (t/reader :json)
            response (<! (http/get (page-url page)))
            pics     (t/read r (:body response))]
        (reset! state pics))))

(defonce displayed-pics (r/atom nil))
(defonce page-num (r/atom 1))

(defn next-page! [pics page-num]
  (swap! page-num inc)
  (get-page! @page-num pics))

(defn prev-page! [pics page-num]
  (swap! page-num dec)
  (get-page! @page-num pics))

(get-page! @page-num displayed-pics)

(defn prev-button [pics pg]
  (when (> @pg 1)
    [:input {:type "button"
             :class "prev"
             :access-key "h"
             :value "⟵ Previous page"
             :on-click #(prev-page! pics pg)}]))

(defn next-button [pics pg]
  (when (= (count @pics) 5)
    [:input {:type "button"
             :class "next"
             :access-key "l"
             :value "Next page ⟶"
             :on-click #(next-page! pics pg)}]))


(defn make-comments [state]
  (map index/pic-and-comment @state))

(defn gallery []
  [:main#pagination
   [:div
    (prev-button displayed-pics page-num)
    (next-button displayed-pics page-num)]
   [:section (make-comments displayed-pics)]])

(r/render [gallery]
          (js/document.getElementById "app"))
