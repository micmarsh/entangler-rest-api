(ns movienight.handler
    (:use compojure.core
        movienight.auth
        [marshmacros.coffee :only [cofmap]])
    (:require [compojure.handler :as handler]
        [compojure.route :as route]
        [liberator.core :refer [resource defresource]]
        [liberator.dev :refer [wrap-trace]] ))

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
(defn no-email-or-password [context]
    (let [params (get-in context [:request :params])]
                (or (nil? (:email params))
                    (nil? (:password params)))))

(defresource signup-or-login [response-function]
           ;TODO: still need good error handling in post!
                    ; see what happens when exception is thrown 
                    ; (this can be done below)
    :available-media-types ["application/json"]
    :allowed-methods [:post]
    :malformed? no-email-or-password
    :post! (fn [context]
        (let [params (get-in context [:request :params])]
            (-> params
                response-function
                build-response)))
    :handle-created :body)

(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] ())
    (POST "/videos" {params :params} ())
    (POST "/signup" [] (signup-or-login signup))
        ;(build-response (signup params))  )
    (POST "/login" []
        (signup-or-login (fn [{:keys [email password]}]
            (login email password))))
    (route/resources "/")
    (route/not-found "Not Found"))

(def app
    (-> app-routes
        handler/site))
