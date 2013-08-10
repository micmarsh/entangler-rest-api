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

(defn- get-headers [context]
    (get-in context [:request :headers]))
(defn- get-auth-header [context]
    (let [headers (get-headers context)]
        (or (headers "Authorization")
            (headers "authorization"))))

(defn- created-or-error [context]
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
    :handle-created created-or-error
)

(defresource access-collection
    :available-media-types ["application/json"]
    :allowed-methods [:get :post]
    :authorized? #(auth/authorized? (get-auth-header %))
    :malformed? #(and (:url (get-params %))
                      (:name (get-params %))) ;TODO: this isn't going to work for GET, will
                                        ;need to make it fancy
    :post! (fn [context]
        (let [{:keys [url name]} (get-params context)]
            (d/create! (cofmap url name))))

    :handle-ok (fn [context]
        (let [auth (get-auth-header context)
              params (get-params context)]
               (d/get-many (assoc params :authtoken auth))))

    :handle-created created-or-error
)
