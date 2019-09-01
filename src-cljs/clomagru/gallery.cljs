(ns clomagru.index)

(defn img-tag [pic-uuid]
  ^{:key pic-uuid}
  [:img.userpic {:src (str "/pics/" pic-uuid)}])
