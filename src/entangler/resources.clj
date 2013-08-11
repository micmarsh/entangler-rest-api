(ns entangler.resources
    (:use [marshmacros.coffee :only [cofmap]])
    (:require
        [entangler.auth :as auth]
        [entangler.datastore :as d]
        [liberator.core :refer [resource defresource]]
        [liberator.dev :refer [wrap-trace]] ))

(defn- str->bool [string]
    (if string
        (Boolean/valueOf string)
    ;else
        false))

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
(defn- update-params [context params]
    (let [old-request (:request context)
          new-request (assoc old-request :params params)]
          (assoc context :request new-request)))

(defn- get-headers [context]
    (get-in context [:request :headers]))
(defn- get-auth-header [context]
    (let [headers (get-headers context)]
        (or (headers "Authorization")
            (headers "authorization"))))

(defn- error-or-response [context]
    (let [body (:body context)]
        (if (error? body)
             (liberator.representation/ring-response body)
            ;else
            body)))

(defresource signup-or-login [response-function]
    :available-media-types ["application/json"]
    :allowed-methods [:post]
    :malformed? no-email-or-password
    :post! (fn [context]
        (let [params (get-params context)]
            (-> params
                response-function
                build-response)))
    :handle-created error-or-response
)

(defn- check-auth-header [context]
    (-> context
        get-auth-header
        auth/authorized?))

(defn- datastore-function [function]
    (fn [context]
        (let [auth (get-auth-header context)
              params (get-params context)
              authed-params (assoc params :authtoken auth)]
              (function authed-params))))
(defn- wrapped-in-body [function]
    (fn [context] {:body
        ((datastore-function function) context)}))

(defresource access-collection
    :available-media-types ["application/json"]
    :allowed-methods [:get :post]
    :authorized? check-auth-header
    :malformed? #(and (:url (get-params %))
                      (:name (get-params %)))
                      ;TODO: pretty sure test case is letting through crap hmmm
    :post! (wrapped-in-body d/create!)

    :handle-ok (datastore-function d/get-many )

    :handle-created error-or-response
)

(defresource single-particle
    :available-media-types ["application/json"]
    :allowed-methods [:get :put :delete]
    :authorized? check-auth-header

    :handle-ok (datastore-function d/get-one)

    :put! (fn [context]
            (let [params (get-params context)
                  shareto (:shareto params)]
                (if (contains? params :shareto)
                    base-fn )) )
    :delete! (wrapped-in-body d/delete!)

    :handle-created error-or-response
    :handle-accepted error-or-response
    :handle-no-content error-or-response
)
