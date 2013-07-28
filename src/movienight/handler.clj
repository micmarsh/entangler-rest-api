(ns movienight.handler
    (:use compojure.core
        movienight.auth
        [marshmacros.coffee :only [cofmap]])
    (:require [compojure.handler :as handler]
        [compojure.route :as route]
        [liberator.core :refer [resource defresource]] ))

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
    (POST "/signup" [] 
        (resource :available-media-types ["application/json"]
                  :allowed-methods [:post]
                  :malformed? (fn [context]
                    (let [params (get-in context [:request :params])]
                        (or (nil? (:email params))
                            (nil? (:password params)))))
                  :post! (fn [context]
                    (let [params (get-in context [:request :params])
                          response (build-response (signup params))]
                          (println response)
                          response )
                  :handle-created #(println %))
                ))
        ;(build-response (signup params))  )
    (POST "/login" {params :params} 
        (let [{:keys [email password]} params]
            (build-response (login email password))))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (handler/site app-routes))
