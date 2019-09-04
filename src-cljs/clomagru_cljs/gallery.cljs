(ns clomagru.index)

(defn comment-form [pic-uuid]
  [:form {:method "post"
          :action "/comment-picc"}
   [:fieldset
    [:legend "Thoughts?"]
    [:input {:type "text"
             :maxlength "140"
             :name "comment"}]
    [:input {:value pic-uuid
             :type "hidden"
             :name "pic-uuid"}]
    [:input {:type "submit"}]]])

(defn img-tag [pic-uuid]
  ^{:key pic-uuid}
  [:img {:id (gensym pic-uuid)
         :src (str "/pics/" pic-uuid)}])

(defn print-comment [comment-map]
  (let [poster (:comments/poster comment-map)
        comment (:comments/comment comment-map)]
    ^{:key (gensym comment-map)}
    [:li
     [:p [:strong poster] ": " comment]]))

(defn pic-and-comment [pic-map]
  (let [pic-uuid (:files/id pic-map)]
    [:article
     [:div {:class "column"}
      [:p [:em (:files/username pic-map)]]
      (img-tag pic-uuid)
      [:p [:time (:files/date pic-map)]]]
     [:div {:class "comments"}
      [:ul
       (map print-comment (:files/comments pic-map))]
      (comment-form pic-uuid)]]))
