(ns movienight.handler
    (:use compojure.core
        [marshmacros.coffee :only [cofmap]])
    (:require [compojure.handler :as handler]
        [compojure.route :as route]))

(defn- str->bool [string]
    (if string
        (Boolean/valueOf string)
    ;else
        false))

(def ALL_MESSAGE "you got all the videos")
(def FAKE_URL "http://url.lulz" )

(defn- handle-post [params]
    (let [{:keys [url convert]} params]
         (if (str->bool convert)
                (str "converting and uploading the video " url)
            ;else
                (str "upload the video " url))))

(defn- handle-single-video [id]
    (let [base (cofmap id)
          with-url (assoc base :url FAKE_URL)]
        {:body with-url}))

(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] (handle-single-video id))
    (POST "/videos" {params :params} (handle-post params))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
