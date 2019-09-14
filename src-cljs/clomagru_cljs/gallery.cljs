(ns clomagru.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as t]
            [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! put!]]))

(defn send-comment! [comment pic-uuid chan]
  (go (let [r (t/reader :json)
            response (<! (http/post "/comment-picc" {:form-params
                                                     {:comment comment
                                                      :pic-uuid pic-uuid}}))
            sent-comment (t/read r (:body response))]
        (put! chan sent-comment))))

(defn comment-form [pic-uuid chan]
  (let [val (r/atom "")]
    (fn []
      [:form 
       [:fieldset
        [:legend "Thoughts?"]
        [:input {:type "text"
                 :auto-complete "off"
                 :value @val
                 :max-length "140"
                 :on-change #(reset! val (-> % .-target .-value))
                 :on-key-down #(case (.-which %)
                                 13 (send-comment! @val pic-uuid chan)
                                 nil)
                 :name "comment"}]
        [:input {:value pic-uuid
                 :type "hidden"
                 :name "pic-uuid"}]
        [:input {:type "button"
                 :value "Submit"
                 :on-click #(do
                              (send-comment! @val pic-uuid chan)
                              (reset! val ""))}]]])))

(defn img-tag [pic-uuid]
  ^{:key pic-uuid}
  [:img {:id (gensym pic-uuid)
         :src (str "/pics/" pic-uuid)}])

(defn print-comment [comment-map]
  (let [poster  (:comments/poster  comment-map)
        comment (:comments/comment comment-map)]
    ^{:key (gensym comment-map)}
    [:li [:strong poster]  [:span comment]]))

(defn like-pic! [pic-id likes-atom]
  (go
    (let [response
          (<! (http/patch (str "http://localhost:3000/pics/" pic-id)))
          status (:status response)]
      (cond
        ; (= status 403) (swap! likes-atom dec) ;; need to dec in backend
        (= status 200) (swap! likes-atom inc)))))

(defn likes-component [pic-map]
  (let [likes (r/atom (:files/likes pic-map))]
    (fn []
      [:small
       {:on-click
        #(like-pic! (:files/id pic-map) likes)} ;; needs visual feedback
       @likes " ♥"])))

(defn pic [pic-map]
  [:div {:class "column"}
   [:p [:strong (:files/username pic-map)]]
   (img-tag (:files/id pic-map))
   [:p {:style {:display "flex" :justify-content "space-evenly"}}
    [likes-component pic-map]
    [:time (:files/date pic-map)]]])

(defn card [pic-map chan]
  (let [pic-uuid (:files/id pic-map)
        likes-atom (r/atom (:files/likes pic-map))]
    ^{:key pic-uuid}
    [:article {:class "card"}
     (pic pic-map)
     [:div {:class "comments"}
      [:ul (map print-comment (:files/comments pic-map))]
      [comment-form pic-uuid chan]]]))
