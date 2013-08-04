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
    (let [with-nils (c/cofmap name url timestamp)
          unmerged  (map 
                (fn [[key value]] (if value {key value} {}))
                    with-nils)]
          (apply merge unmerged)))

(def kinvey->entangler
    (utils/get-map-converter     
        (let [without-time (apply merge 
            (for [-key [:name :url :_id]]
                {-key #(k/get-attr % -key)}))]
        (assoc without-time
            :timestamp (fn [kinvey]
                        (or (k/get-attr kinvey :timestamp)
                            (let [kmd (k/get-attr kinvey :_kmd)]
                                ( kmd "ect"))))))))
 
(defn create! [params]
    (let [token (:authtoken params)
          coll (get-collection token)
          kinvey-entity (k/new-entity coll (sanitize params))]
          (kinvey->entangler kinvey-entity)))


(defn update! [params]
    (let [{:keys [authtoken _id]} params
          coll (get-collection authtoken)
          entity (k/get-entity coll _id)
          new-entity (k/update entity (sanitize params))]
          (kinvey->entangler new-entity)))

(defn delete! [params]
    (let [{:keys [authtoken _id]} params
            coll (get-collection authtoken)]
            (k/delete-entity coll _id)))
