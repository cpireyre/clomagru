(ns clomagru-cljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clomagru.index :as index]
            [cognitect.transit :as t]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<! take!]]))

(def chan (async/chan))

(defn page-url [pagenum]
  (str "http://10.12.7.10:3000/page/" pagenum))

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


(defn make-cards [state chan]
  (map #(index/card % chan) @state))

(defn get-map-to-update [map to-update]
  (first (filter #(= % to-update) map)))

(defn update-map [to-update comment]
  (assoc-in to-update [:files/comments]
            (conj (:files/comments to-update)
                    {:comments/comment (:comment comment)
                     :comments/poster (:poster-name comment)})))

(defn remove-map [state rmap]
  (remove #(= % rmap) state))

(defn merge-async-comment [state comment]
  (let [pic-uuid (:pic_uuid comment)
        map-to-update (-> (filter #(= (:files/id %) pic-uuid) @state)
                          (first))]
    (prn map-to-update)
    (prn comment)
    (prn (type @state))
    (swap! state #(-> (merge % (update-map map-to-update comment))
                      (remove-map map-to-update)))))

(go (loop []
      (let [comment (<! chan)]
        (merge-async-comment displayed-pics comment))
      (recur)))

(defn gallery []
  [:main#pagination
   [:div
    (prev-button displayed-pics page-num)
    (next-button displayed-pics page-num)]
   [:section (make-cards displayed-pics chan)]])

(r/render [gallery]
          (js/document.getElementById "app"))
