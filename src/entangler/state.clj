(ns entangler.state
    (:use [marshmacros.coffee :only [cofmap]]))

(def good-auth (atom { }))
(def bad-auth (atom { }))

(defn set-good-auth! [token]
    (swap! bad-auth #(dissoc % token))
    (swap! good-auth #(assoc % token true)))
(defn set-bad-auth! [token]
    (swap! good-auth #(dissoc % token))
    (swap! bad-auth #(assoc % token true)))

(defn good-auth? [token]
    (@good-auth token))

(defn bad-auth? [token]
    (@bad-auth token))

(def sockets (atom { }))
(defn- get-sockets [sockets id]
    (or (sockets id) #{}))

(defn- update-fn [{:keys [socket _id modifier]}]
    (fn [sockets]
        (let [sockets-set (get-sockets sockets _id)
                ;TODO this set should probably be a map
                ;of some unique origin identifier to the socket
              new-sockets (modifier sockets-set socket)]
            (assoc sockets _id new-sockets))))

(defn- get-sockets-updater [modifier]
    (fn [{:keys [_id socket]}]
        (swap! sockets (update-fn (cofmap _id socket modifier)))))

(def add-socket! (get-sockets-updater conj))
(def remove-socket! (get-sockets-updater disj))

