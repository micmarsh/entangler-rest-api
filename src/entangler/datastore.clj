(ns entangler.datastore 
    (:require [entangler.auth :as auth]
              [kinvey.core :as k]
              [marshmacros.coffee :as c]
              [entangler.utils :as utils]))

(defn- get-collection [token]
    (-> auth/kinvey-app
        (k/load-user (utils/kinvey-auth token))
        (k/make-collection "things")))

(defn- sanitize [{:keys [name url timestamp]}]
    (c/cofmap name url timestamp))

(def kinvey->entangler
    (utils/get-map-converter     
        (let [without-time (apply merge 
            (for [-key [:name :url :_id]]
                {-key #(k/get-attr % -key)}))]
        (assoc without-time
            :timestamp (fn [kinvey]
                        (let [kmd (k/get-attr kinvey :_kmd)]
                            ( kmd "ect")))))))
 
(defn create! [params]
    (println params)
    (let [token (:authtoken params)
          coll (get-collection token)
          kinvey-entity (k/new-entity coll (sanitize params))]
          (println kinvey-entity)
          (kinvey->entangler kinvey-entity)))

