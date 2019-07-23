(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :as c]
            [clomagru.page :as p])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] "coming soon")
  (route/not-found "<h1>404</h1>"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Starting server...")
  (run-jetty (wrap-reload #'app) {:port 3000})) ;; live reload whilst developping
