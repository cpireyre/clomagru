(ns clomagru-cljs.camera
  (:require [reagent.core :as r]))

(defonce state (r/atom '()))

(defn take-picture! [video canvas]
  (let [context (.getContext canvas "2d")]
    (.drawImage context video 0 0 320 240)
    (let [data (.toDataURL canvas "image/png")]
      (swap! state #(conj % data)))))

(defn startup! []
  (let [video       (js/document.getElementById "video")
        width 320
        height 240
        canvas      (js/document.getElementById "canvas")
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
                       #(take-picture! video canvas))))

(defn camera []
  [:div {:class "camera"}
   [:video#video "No video available at this time."]
   [:br]
   [:button#startbutton
    {:style {:margin-bottom "1em"}}
    "Take picc."]])

(defn canvas []
  [:div
   [:canvas#canvas {:style {:display "none"}}]])

(defn send-webcam-pic-form []
  (let [canvas (js/document.getElementById "canvas")]
    [:button "Submit this photo."]))

(defn display-pics [pics]
  [:section {:id "previews"}
   [:h3 "Previous photos"]
   [:ul 
    (for [data pics]
      [:li
       [:img {:src data
              :alt "Previous photo"
              :class "preview"}]])]])

(defn page []
  [:section#camera 
   (camera)
   (canvas)
   (when (seq @state)
     [:div
      [:img {:alt "Current photo"
             :src (first @state)}]
      [:br]
      (send-webcam-pic-form)
     [:br]
     (display-pics (rest @state))])])

(r/render [page]
          (js/document.getElementById "app"))

(startup!)
