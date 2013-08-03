(ns entangler.datastore 
    (:require [entangler.auth :as auth]
              [kinvey.core :as k]
              [marshmacros.coffee :as c]))

(defn- get-collection [token]
    (-> auth/kinvey-app
        (k/load-user token)
        (k/make-collection "things")))

(defn- sanitize [{:keys [name url timestamp]}]
    (c/cofmap name url timestamp))

(defn- kinvey->entangler [entity]
    (let [attributes [:name :url]
          ])
 
(defn create! [params]
    (let [token (:authtoken params)
          coll (get-collection token)
          kinvey-entity (new-entity coll (sanitize params))]
          (kinvey->entangler kinvey-entity)))

