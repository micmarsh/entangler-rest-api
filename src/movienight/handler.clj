(ns movienight.handler
    (:use compojure.core)
    (:require [compojure.handler :as handler]
        [compojure.route :as route]))

(defroutes app-routes
    (GET "/videos" [] "you got all the videos")
    (GET "/videos/:id" [id] (str "you got a video " id) )
    (POST "/videos" {{url :url} :params} (str "upload the video " url))
        (route/resources "/")
        (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
