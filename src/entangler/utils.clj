(ns entangler.utils
    (:require [kinvey.core :as k]))

(def KINVEY_PREFIX "Kinvey ")
(def ENTANGLER_PREFIX "Entangler ")

(defn entangler-auth [kinvey-user]
    (let [token (k/get-auth kinvey-user)]
        (apply str 
            ENTANGLER_PREFIX
            (drop (count KINVEY_PREFIX) token))))

(defn kinvey-auth [entangler-token]
  (->> entangler-token
    (drop (count ENTANGLER_PREFIX))
    (apply str KINVEY_PREFIX)))

(defn get-map-converter [translator]
    (fn [from]
        (let [unmerged (for [[-key fn] translator]
                {-key (fn from)})]
        (apply merge unmerged))))


