(ns movienight.handler
    (:use compojure.core
        [marshmacros.coffee :only [cofmap]]
        [crypto.random :only [url-part]])
    (:require [compojure.handler :as handler]
        [compojure.route :as route]))

(defn- str->bool [string]
    (if string
        (Boolean/valueOf string)
    ;else
        false))

(def rand-string url-part)
(def ALL_MESSAGE "you got all the videos")
(def FAKE_URL "http://url.lulz")

(def fake-db (atom #{}) )

(defn- build-movie-data [params]
    (let [{:keys [url convert]} params
          id (url-part 7)
          base-data (cofmap url id)
          return-data (assoc base-data :convert (str->bool convert))]
        {:body return-data }))

(defn- handle-post [params]
    (let [data (build-movie-data params)
          body (:body data)]
        (swap! fake-db #(conj % body))
        data))

(defn- find-single-video [id]
    (let [base (cofmap id)]
        (if (contains? @fake-db id)
            {:body base}
        ;else
            {:body "Video doesn't exist"
             :status 401})))

(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] (find-single-video id))
    (POST "/videos" {params :params} (handle-post params))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
