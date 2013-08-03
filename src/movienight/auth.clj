(ns movienight.auth 
    (:use movienight.secrets
        [marshmacros.coffee :only [cofmap]])
    (:require [kinvey.core :as k]))

(def kinvey-app (k/initialize-app APP_KEY APP_SECRET))
(def KINVEY_PREFIX "Kinvey ")
(def ENTANGLER_PREFIX "Entangler ")

(def good-auth (atom { }))
(def bad-auth (atom { }))
(defn- set-good-auth! [token]
    (swap! bad-auth #(dissoc % token))
    (swap! good-auth #(assoc % token true)))
(defn- set-bad-auth! [token]
    (swap! good-auth #(dissoc % token))
    (swap! bad-auth #(assoc % token true)))

(declare kinvey-auth )
(defn- auth-ping! [token]
    (let [kinvey-token (kinvey-auth token)
          user (k/load-user kinvey-app kinvey-token)
          collection (k/make-collection user "ping")
          test-entity (k/new-entity collection {:test true})]
          (k/kinvey-object? test-entity)))

(defn authorized? [token]
    (and  (not (@bad-auth token))
          (or (@good-auth token)
              (auth-ping! token))))

(defn- get-attr-adder [kinvey-user]
    (fn [so-far, attr]
        (assoc so-far attr 
            (k/get-attr kinvey-user attr))))

(defn- entangler-auth [kinvey-user]
    (let [kinvey-auth (k/get-auth kinvey-user)]
        (apply str 
            ENTANGLER_PREFIX
            (drop (count KINVEY_PREFIX) kinvey-auth))))

(defn- kinvey-auth [entangler-token]
  (->> entangler-token
    (drop (count ENTANGLER_PREFIX))
    (apply str KINVEY_PREFIX)))


(defn- convert-user [kinvey-user]
    (let [attributes [:email, :firstName, :lastName, :_id]
        user-no-auth (reduce (get-attr-adder kinvey-user)
          { } attributes)
        user-auth (entangler-auth kinvey-user)]
            (set-good-auth! user-auth)
            (assoc user-no-auth :authtoken user-auth)))

(defn- user-from-kinvey [response]
    (if (k/kinvey-object? response)
      (convert-user response)
      response))

(defn signup [args]
    (->> (:email args)
    (assoc args :username)
    (k/signup kinvey-app)
    (user-from-kinvey)))

(defn login [email password]
    (user-from-kinvey
        (k/login kinvey-app email password)))

;TODO: signup: take first, last, email, password
;       login: email, password
;       
;   general problem: there a a lot of things going on here, mainly conversion
;   and syncing commands, that require a quick auth. Solution: a kind of cache in memory
;   for super fast-checking that checks kinvey if not found before rejecting.
;   