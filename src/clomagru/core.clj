(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            ;[compojure.coercions :as c]
            [clomagru.page :as p]
            [clomagru.db :as db])
  (:gen-class))

(defn destructure-form-input [form-params]
  {:username (get form-params "username")
   :email (get form-params "email")
   :password (get form-params "password")})

(defn make-account [user-info]
  (do
    (let [credentials (destructure-form-input user-info)]
      (db/create-account credentials)
    (str "Created account for " (:username credentials) "."))))

(defroutes app-routes
  (GET "/" [] "coming soon")
  (GET "/list" [] (db/pretty-print-accounts))
  (GET "/register" [] (p/register-page))
  (POST "/make-account" req (make-account (:form-params req)))
  (route/not-found "<h1>404</h1>"))

(def app
  (wrap-defaults app-routes
                 (assoc-in site-defaults [:security :anti-forgery] false)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Starting server...")
  (run-jetty (wrap-reload #'app) {:port 3000})); live reload whilst developping
