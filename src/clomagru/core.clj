(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer :all]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [clomagru.page :as p]
            [clomagru.upload :as upload]
            [clomagru.gallery :as gallery]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.key :as key])
  (:gen-class))

(defroutes app-routes
  (GET  "/"              req      (p/index-page req))
  (GET  "/login"         req      (p/login-page req))
  (GET  "/logout"        req      (login/wipe-session))
  (POST "/login"         req      (login/handler req))
  (GET  "/pics/:uuid"    [uuid]   (gallery/get-image uuid))
  (GET  "/gallery/:user" req      (gallery/get-user-gallery req))
  (GET  "/camera"        req      (p/camera-page req))
  (GET  "/list"          req      (p/list-accounts req
                                                   (db/select-all-accounts)))
  (GET  "/register"      req      (p/register-page req))
  (POST "/make-account"  req      (do
                                    (db/make-account (:form-params req))
                                    (p/list-accounts req (db/select-all-accounts))))
  (POST "/upload-picc"   req      (upload/save-image!
                                    (get-in req [:session :uuid])
                                    (get (:multipart-params req) "file")))
  (route/not-found "<h1>404</h1>")) ;; TODO: make a real 404 page

(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                         (assoc-in [:session :store]
                                   (cookie-store {:key key/store-key}))
                         (assoc-in [:security :anti-forgery] false)))))

(defn -main
  "Turns on web server."
  [& args]
  (println "Starting server.")
  (run-jetty (wrap-reload #'app) {:port 3000}));live reload whilst developping
