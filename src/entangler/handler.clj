(ns entangler.handler
    (:use [compojure.core :only [ANY POST defroutes]]
        ring.middleware.cors
        org.httpkit.server
        [entangler.resources :only
                [access-collection single-particle signup-or-login]])
    (:require [compojure.handler :as handler]
                [compojure.route :as route]
                [entangler.auth :as auth]
                [ring.middleware.reload :as reload]))

(defroutes app-routes
    (ANY "/particles" [] access-collection)
    (ANY "/particles/:_id" [] single-particle)
    (POST "/signup" [] (signup-or-login auth/signup))
        ;(build-response (signup params))  )
    (POST "/login" []
        (signup-or-login (fn [{:keys [email password]}]
            (auth/login email password))))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (-> app-routes
        handler/site
        reload/wrap-reload
     (wrap-cors
      :access-control-allow-origin #".+")))
