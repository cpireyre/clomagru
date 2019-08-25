(ns clomagru.email
  (:require [postal.core :as p]
            [clomagru.key :as key]))

(defn send-mail [email-to]
  (p/send-message key/smtp-params
                  {:from "clomagru42@gmail.com"
                   :to email-to
                   :subject "henlo"
                   :body "HENLO"}))
