(ns movienight.handler
    (:use compojure.core)
    (:require [compojure.handler :as handler]
        [compojure.route :as route]))

(defn- str->bool [string]
    (if string
        (Boolean/valueOf string)
    ;else
        false))

(defn- handle-post [params]
    (let [{:keys [url convert]} params]
         (if (str->bool convert)
                (str "converting and uploading the video " url)
            ;else
                (str "upload the video " url))))

(def ALL_MESSAGE "you got all the videos")


(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] (str "you got a video " id))
    (POST "/videos" {params :params} (handle-post params))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
