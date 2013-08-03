(ns movienight.handler
    (:use compojure.core
        [marshmacros.coffee :only [cofmap]])
    (:require [compojure.handler :as handler]
        [movienight.auth :as auth]
        [movienight.datastore :as d]
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

(defn error? [response]    
    (let [status (:status response)]
        (and status (> status 300))))

(defn build-response [body]
        (cofmap body))

(defn no-email-or-password [context]
    (let [params (get-in context [:request :params])]
                (or (nil? (:email params))
                    (nil? (:password params)))))

(defn- get-params [context]
    (get-in context [:request :params]))

(defresource signup-or-login [response-function]
    :available-media-types ["application/json"]
    :allowed-methods [:post]
    :malformed? no-email-or-password
    :post! (fn [context]
        (let [params (get-params context)]
            (-> params
                response-function
                build-response)))
    :handle-created (fn [context] 
        (let [body (:body context)]
            (if (error? body)
                 (liberator.representation/ring-response body)
                ;else 
                body)))
)

(defresource access-collection 
    :available-media-types ["application/json"]
    :allowed-methods [:get :post]
    :authorized? #(auth/authorized? (:authtoken (get-params %)))
    :malformed? #(and (:url (get-params %))
                      (:name (get-params %))) ;TODO: this isn't going to work for GET, will 
                                        ;need to make it fancy
    :post! (fn [context]
        (let [{:keys [url name]} (get-params context)]
            (d/create! (cofmap url name))))
)


(defroutes app-routes
    (GET "/videos" [] ALL_MESSAGE)
    (GET "/videos/:id" [id] ())
    (POST "/videos" {params :params} ())
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
