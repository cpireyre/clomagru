(ns clomagru.index)

(defn img-tag [pic-uuid]
  ^{:key pic-uuid}
  [:img {:src (str "/pics/" pic-uuid)}])
