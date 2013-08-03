(ns entangler.handler
    (:use compojure.core
        [entangler.resources :only 
                [access-collection signup-or-login]])
    (:require [compojure.handler :as handler]
                [compojure.route :as route]
                [entangler.auth :as auth]))

(defroutes app-routes
    (ANY "/particles" [] access-collection)
    (GET "/particles/:id" [id] ())
    (POST "/signup" [] (signup-or-login auth/signup))
        ;(build-response (signup params))  )
    (POST "/login" []
        (signup-or-login (fn [{:keys [email password]}]
            (auth/login email password))))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (-> app-routes
        handler/site))
