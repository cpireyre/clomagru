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

(get-page! @page-num displayed-pics)

(defn gallery []
  [:main#latest
   [:input {:type "button" :value "next page"
            :on-click #(next-page! displayed-pics page-num)}]
   (map (comp index/img-tag :files/id) @displayed-pics) ])

(r/render [gallery]
          (js/document.getElementById "app"))
