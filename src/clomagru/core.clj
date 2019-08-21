(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer :all]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [clomagru.page :as p]
            [clomagru.upload :as upload]
            [clomagru.gallery :as gallery]
            [clomagru.db :as db])
  (:gen-class))

(defroutes app-routes
  (GET "/"              []      (p/index-page))
  (GET "/login"         []      (p/login-page))
  (POST "/login"        req     (str req))
  (GET "/pics/:uuid"    [uuid]  (gallery/get-image uuid))
  (GET "/gallery/:user" [user]  (gallery/get-user-gallery user))
  (GET "/camera"        []      (p/camera-page))
  (GET "/list"          []      (p/list-accounts (db/select-all-accounts)))
  (GET "/register"      []      (p/register-page))
  (GET "/session"       req     (str req))
  (POST "/make-account" req (do
                              (db/make-account (:form-params req))
                              (p/list-accounts (db/select-all-accounts))))
  (POST "/upload-picc"  req (upload/save-image! "guy garvey" (get (:multipart-params req) "file")))
  (route/not-found "<h1>404</h1>"))

(def app
  (-> app-routes
      wrap-session
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)))))

(defn -main
  "Turns on web server."
  [& args]
  (println "Starting server.")
  (run-jetty (wrap-reload #'app) {:port 3000}));live reload whilst developping
