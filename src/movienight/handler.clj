(ns movienight.handler
    (:use compojure.core
        movienight.auth
        [marshmacros.coffee :only [cofmap]])
    (:require [compojure.handler :as handler]
        [compojure.route :as route]))

(defn- str->bool [string]
    (if string
        (Boolean/valueOf string)
    ;else
        false))

(def ALL_MESSAGE "you got all the videos")
(def FAKE_URL "http://url.lulz")

(defn build-response [body]
    (if (:error body)
        {:body (dissoc body :status)
         :status (body :status)}
    ;else
        (cofmap body)))

(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] ())
    (POST "/videos" {params :params} ())
    (POST "/signup" {params :params} 
        (build-response (signup params))  )
    (POST "/login" {params :params} 
        (let [{:keys [email password]} params]
            (build-response (login email password))))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
