(ns clomagru.index
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as t]
            [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn send-comment! [comment pic-uuid]
  (go (let [r (t/reader :json)
            response (<! (http/post "/comment-picc" {:form-params
                                                     {:comment comment
                                                      :pic-uuid pic-uuid}}))
            sent-comment (t/read r (:body response))]
        (prn sent-comment))))

(defn comment-form [pic-uuid]
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
                                 13 (send-comment! @val pic-uuid)
                                 nil)
                 :name "comment"}]
        [:input {:value pic-uuid
                 :type "hidden"
                 :name "pic-uuid"}]
        [:input {:type "button"
                 :value "Submit"
                 :on-click #(do
                              (send-comment! @val pic-uuid)
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

(defn pic-and-comment [pic-map]
  (let [pic-uuid (:files/id pic-map)]
    ^{:key pic-uuid} [:article {:class "card"}
     [:div {:class "column"}
      [:p [:strong (:files/username pic-map)]]
      (img-tag pic-uuid)
      [:p [:time (:files/date pic-map)]]]
     [:div {:class "comments"}
      [:ul (map print-comment (:files/comments pic-map))]
      [comment-form pic-uuid]]]))
