(ns clomagru.email
  (:require [postal.core :as p]
            [clomagru.key :as key]
            [clomagru.log :as log]))

(defn send-mail! [recipient subject body]
  (let [mail-return-code (p/send-message key/smtp-params
                                         {:from    "clomagru42@gmail.com"
                                          :to      recipient
                                          :subject subject
                                          :body    body})]
    (log/timelog-stdin "Sending e-mail to" recipient (str ": " mail-return-code))))
