(ns entangler.datastore 
    (:require [entangler.auth :as auth]
              [kinvey.core :as k]
              [marshmacros.coffee :as c]
              [entangler.utils :as utils]))

(defn- get-collection [token]
    (-> auth/kinvey-app
        (k/load-user (utils/kinvey-auth token))
        (k/make-collection "things")))


(declare entangler->kinvey )
(defn- sanitize [{:keys [name url timestamp who]}]
    (let [with-nils (c/cofmap name url timestamp who)
          unmerged  (map 
                (fn [[key value]] (if value {key value} {}))
                    with-nils)]
          (->> unmerged
          (apply merge)
          entangler->kinvey)))

(defn- who->acl [who]
    {"creator" (who 0)
     "r" who
     "w" who})

(defn- acl->who [acl]
  (let [creator (acl "creator")
        r (or (acl "r") [creator])
        w (or (acl "w") [creator])
        rw (apply conj r w)]
      (-> (conj rw creator) set vec )))

(def to-entangler     
  (utils/get-map-converter     
        (let [without-time (apply merge 
            (for [-key [:name :url :_id]]
                {-key #(k/get-attr % -key)}))]
        (assoc without-time
            :timestamp (fn [kinvey]
                        (or (k/get-attr kinvey :timestamp)
                            (let [kmd (k/get-attr kinvey :_kmd)]
                                ( kmd "ect"))))
            :who (fn [kinvey] 
                    (let [acl (k/get-attr kinvey :_acl)]
                      (vec (acl->who acl))))))))

(defn- kinvey->entangler [entity]
    (if (k/kinvey-object? entity)
      (to-entangler entity)
    entity))

(def entangler->kinvey
    (utils/get-map-converter {
      :name :name
      :url :url
      :_id :_id
      :timestamp :timestamp
      :_acl #(let[who (:who %)]
                (if who
                  (who->acl who)
                  {"creator" (:_id %)}))  
      }))
 
(defn create! [params]
    (let [token (:authtoken params)
          coll (get-collection token)
          kinvey-entity (k/new-entity coll (sanitize params))]
          (kinvey->entangler kinvey-entity)))

(defn- get-it [params]
    (let [{:keys [authtoken _id]} params
        coll (get-collection authtoken)]
        (k/get-entity coll _id)))

(defn get-one [params]
    (kinvey->entangler (get-it params)))

(defn get-many [params]
    (let [token (:authtoken params)
          coll (get-collection token)
          response (k/get-entities 
            coll { } {:limit (:limit params)})]
          (if (= (type response) clojure.lang.PersistentArrayMap)
            response
          (map kinvey->entangler response))))
  
(defn update! [params]
    (let [entity (get-it params)
          new-entity (k/update entity (sanitize params))]
          (kinvey->entangler new-entity)))

(defn delete! [params]
    (let [{:keys [authtoken _id]} params
            coll (get-collection authtoken)]
            (k/delete-entity coll _id)))

(defn share! [params]
    (let [{:keys [_id email]} params
          entity (get-one params)
          who (:who entity)
          with-id (conj who _id)]
        (-> {:who with-id}
          (merge params)
           update!)))



