(ns clomagru.upload
  (:require [clomagru.db :as db])
  (:import [java.io ByteArrayOutputStream FileInputStream]))

(defn file->byte-array [x]
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

(defn save-image! [user {:keys [tempfile filename content-type]}]
  (db/save-file! {:owner user
                  :type  content-type
                  :data  (file->byte-array tempfile)})
  (str "saved file?"))
