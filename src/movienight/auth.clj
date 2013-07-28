(ns movienight.auth 
    (:use movienight.secrets
        [marshmacros.coffee :only [cofmap]])
    (:require [kinvey.core :as k]))

(def kinvey-app (k/initialize-app APP_KEY APP_SECRET))

(def good-auth (atom { }))
(def bad-auth (atom { }))
(defn- set-good-auth! [token]
    (swap! bad-auth #(dissoc % token))
    (swap! good-auth #(assoc % token true)))
(defn- set-bad-auth! [token]
    (swap! good-auth #(dissoc % token))
    (swap! bad-auth #(assoc % token true)))


(defn- get-attr-adder [kinvey-user]
    (fn [so-far, attr]
        (assoc so-far attr 
            (k/get-attr kinvey-user attr))))
(defn- entangler-auth [kinvey-user]
    (let [kinvey-auth (k/get-auth kinvey-user)]
        (apply str 
            "Entangler "
            (drop (count "Kinvey ") kinvey-auth))))
(defn- user-from-kinvey [kinvey-user]
    (let [attributes [:email, :firstName, :lastName]
          user-no-auth (reduce (get-attr-adder kinvey-user)
            { } attributes)
          user-auth (entangler-auth kinvey-user)]
              (set-good-auth! user-auth)
              (assoc user-no-auth :authtoken user-auth)))


(defn- check-for-user-error [{:keys [email password]}]
    (cond (nil? password)
        "Please provide a password"
          (nil? email)
        "Please provide a valid email address"
        :else
         nil))

(defn- signup-or-login [args no-errors]
    (let [error-message (check-for-user-error args)]
        (if error-message
           {:error error-message
            :status 401}
        ;else
            (no-errors args))))

(defn signup [args]
    (signup-or-login args 
                #(->> (:email %)
                (assoc % :username)
                (k/signup kinvey-app)
                (user-from-kinvey))))
(defn login [email password]
    (signup-or-login 
        (cofmap email password)
                (fn [{:keys [email password]}]
                    (user-from-kinvey
                        (k/login kinvey-app email password)))))

;TODO: signup: take first, last, email, password
;       login: email, password
;       
;   general problem: there a a lot of things going on here, mainly conversion
;   and syncing commands, that require a quick auth. Solution: a kind of cache in memory
;   for super fast-checking that checks kinvey if not found before rejecting.
;   