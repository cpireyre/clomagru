(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.multipart-params.byte-array
             :refer [byte-array-store]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            ;[compojure.coercions :as c]
            [clomagru.page :as p]
            [clomagru.db :as db])
  (:gen-class))

(defroutes app-routes
  (GET "/"              []  (p/index-page))
  (GET "/camera"        []  (p/camera-page))
  (GET "/list"          []  (p/list-accounts (db/select-all-accounts)))
  (GET "/register"      []  (p/register-page))
  (POST "/make-account" req (do
                              (db/make-account (:form-params req))
                              (p/list-accounts (db/select-all-accounts))))
  (POST "/upload-picc"  req (str "<pre>"
                                 (with-out-str (clojure.pprint/pprint req))
                                 "</pre>"))
  (route/not-found "<h1>404</h1>"))

(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                      (assoc-in [:security :anti-forgery] false)
                      (assoc-in [:params :multipart] {:store byte-array-store})))))

(defn -main
  "Turns on web server."
  [& args]
  (println "Starting server...")
  (run-jetty (wrap-reload #'app) {:port 3000})); live reload whilst developping

(clojure.pprint/pprint site-defaults)
