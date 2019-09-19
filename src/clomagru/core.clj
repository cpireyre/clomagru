(ns clomagru.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :refer :all]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.anti-forgery :refer :all]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer [defroutes GET POST PATCH]]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int as-uuid]]
            [clomagru.page :as p]
            [clomagru.upload :as upload]
            [clomagru.gallery :as gallery]
            [clomagru.db :as db]
            [clomagru.login :as login]
            [clomagru.key :as key]
            [clomagru.token :as token]
            [clomagru.comment :as comment]
            [clomagru.accountsettings :as settings]
            [clomagru.forgot :as forgot])
  (:gen-class))

(defroutes app-routes
  (GET   "/"              req                    (p/index-page req))
  (GET   "/login"         req                    (p/login-page req))
  (POST  "/login"         req                    (login/handler req))
  (GET   "/logout"        req                    (login/wipe-session))
  (GET   "/info"          req                    (p/change-account-info req))
  (PATCH "/info"          req                    (settings/handler req))
  (GET   "/pics/:id"      [id :<< as-uuid :as r] (gallery/get-image r id))
  (PATCH "/pics/:id"      [id :<< as-uuid :as r] (comment/like r id))
  (GET   "/gallery/:user" req                    (gallery/get-user-gallery req))
  (GET   "/users/:user"   req                    (gallery/get-user-gallery req))
  (GET   "/camera"        req                    (p/camera-page req))
  (GET   "/page/:num"     [num :<< as-int]       (gallery/get-gallery-page num))
  (GET   "/list"          req                    (p/list-accounts req))
  (GET   "/users"         req                    (p/list-accounts req))
  (GET   "/register"      req                    (p/register-page req))
  (GET   "/confirm/:tok"  [tok :<< as-uuid]      (token/confirm-account! tok))
  (POST  "/make-account"  req                    (login/make-account-handler req))
  (POST  "/users"         req                    (login/make-account-handler req))
  (POST  "/upload-picc"   req                    (upload/save-image! req))
  (POST  "/pics"          req                    (upload/save-image! req))
  (POST  "/comment-picc"  req                    (comment/post-comment! req))
  (POST  "/reset"         req                    (forgot/handler! req))
  (GET   "/reset/:tok"    [tok :<< as-uuid]      (forgot/reset-pw-handler! tok))
  (route/not-found                               (p/not-found-page)))

(def app
  (-> app-routes
      (wrap-cors :access-control-allow-origin [#".*"] ;; very unsafe
                 :access-control-allow-methods [:get :put :post :patch]
                 :access-control-allow-credentials "true")
      (wrap-defaults (-> site-defaults
                         (assoc-in [:session :store]
                                   (cookie-store {:key key/store-key}))
                         (assoc-in [:security :anti-forgery] false)))))

(def reload-app (wrap-reload #'app))

(defn -main
  "Turns on web server."
  [& args]
  (println "Starting server.")
  (run-jetty reload-app {:port 3000}));live reload whilst developping
