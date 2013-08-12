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
(defn- sanitize [{:keys [name url timestamp who _id]}]
    (let [with-nils (c/cofmap name url timestamp who _id)
          unmerged  (map
                (fn [[key value]] (if value {key value} {}))
                    with-nils)]
          (->> unmerged
          (apply merge))))

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
    "Doesn't convert if it's an error object"
    (if (k/kinvey-object? entity)
      (to-entangler entity)
    entity))
;TODO might be something wrong in here, but
;hard to tell for sure
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

(defn- without-nil-values [hashmap]
  (->> hashmap
    (remove (comp nil? second))
    (into {})))

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
          update-values  (-> params
                      sanitize
                      entangler->kinvey
                      without-nil-values)
          new-entity (k/update entity update-values)]
          (kinvey->entangler new-entity)))

(defn delete! [params]
    (let [{:keys [authtoken _id]} params
            coll (get-collection authtoken)]
            (k/delete-entity coll _id)))

(defn share! [params]
    (let [{:keys [_id shareto]} params
          entity (get-one params)
          who (:who entity)
          with-other (conj who shareto )]
        (-> {:who with-other}
          (merge params)
           update!)))



