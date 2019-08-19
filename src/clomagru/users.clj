(ns clomagru.users
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn not-too-short? [s]
  (let [len (count s)]
    (if (and (> len 6) (< len 256))
      true
      false)))

(def non-empty-string-alphanumeric
  "Generator for non-empty alphanumeric strings"
  (gen/such-that #(not= "" %)
                 (gen/string-alphanumeric)))

(s/def ::non-empty-alphanumeric
  (s/with-gen string? (fn [] non-empty-string-alphanumeric)))

(def email-gen  ;;  h/t: github.com/conan
  "Generator for email addresses."
  (gen/fmap
    (fn [[name host tld]]
      (str name "@" host "." tld))
    (gen/tuple
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric)))

(def email-regex
  #"^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$")

(s/def ::email
  (s/with-gen
    #(re-matches email-regex %)
    (fn [] email-gen)))

(s/def ::username (s/and ::non-empty-alphanumeric #(< (count %) 29)))
(s/def ::password (s/and string? not-too-short?))
(s/def ::user (s/keys :req [::username ::email ::password]))

(defn gen-one-user []
  (gen/generate (s/gen ::user)))
