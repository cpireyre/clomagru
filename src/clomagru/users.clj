(ns clomagru.users
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn not-too-short? [s]
  (let [len (count s)]
    (and (> len 6) (< len 256))))

(def actually-alnum-regex #"^\w+$")
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

(s/def ::username (s/and
                    ::non-empty-alphanumeric
                    #(< (count %) 29)
                    #(re-matches actually-alnum-regex %)))
(s/def ::password (s/and string? not-too-short?))
(s/def ::user (s/keys :req [::username ::email ::password]))

(defn gen-one-user []
  (gen/generate (s/gen ::user)))

(defn valid-user? [credentials]
  (and (s/valid? ::username (:username credentials))
       (s/valid? ::password (:password credentials))
       (s/valid? ::email (:email credentials))))

(defn valid-credentials? [credentials]
  "Same as valid-user? but does not require the ::email key."
  (and (s/valid? ::username (:username credentials))
       (s/valid? ::password (:password credentials))))

(defn valid-username? [name-maybe]
  (s/valid? ::username name-maybe))

