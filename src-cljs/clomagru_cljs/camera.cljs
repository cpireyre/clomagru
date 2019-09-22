(ns clomagru-cljs.camera
  (:require [reagent.core :as r]))

(defn take-picture! [video canvas photo]
  (let [context (.getContext canvas "2d")]
    (.drawImage context video 0 0 320 240)
    (let [data (.toDataURL canvas "image/png")]
      (set! (.-src photo) data)
      (set! (.-style photo)
            "display: initial; width: 320px; height: 240px;"))))

(defn startup! []
  (let [video       (js/document.getElementById "video")
        width 320
        height 240
        canvas      (js/document.getElementById "canvas")
        photo       (js/document.getElementById "photo")
        startbutton (js/document.getElementById "startbutton")]
    (-> (js/navigator.mediaDevices.getUserMedia
          (js-obj "video" true "audio" false))
        (.then #(do (set! (.-srcObject video) %)
                    (.play video))))
    (set! (.-width video) width)
    (set! (.-height video) height)
    (set! (.-width canvas) width)
    (set! (.-height canvas) height)
    (.addEventListener startbutton "click"
                       #(take-picture! video canvas photo))))

(defn camera []
  [:div {:class "camera"}
   [:video#video "No video available at this time."]
   [:br]
   [:button#startbutton "Take picc."]])

(defn canvas []
  [:div
   [:canvas#canvas {:style {:display "none"}}]
   [:div {:class "output"}
    [:img#photo {:alt "Screen capture goes here."
                 :style {:width 320 :height 240
                         :border "3px solid red"
                         :display "none"}}]]])

(defn page []
  [:section
   (camera)
   (canvas)])

(r/render [page]
          (js/document.getElementById "app"))

(startup!)
