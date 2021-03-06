(ns entangler.sync
    (:use [org.httpkit.server :only
        [with-channel on-close]]
        [marshmacros.coffee :only [cofmap]]))

(defn- get-sockets [sockets id]
    (or (sockets id) #{}))

(defn- update-fn [{:keys [socket _id modifier]}]
    (fn [sockets]
        (let [sockets-set (get-sockets sockets _id)
              new-sockets (modifier sockets-set socket)]
            (if (= (count new-sockets) 0)
                (dissoc sockets _id)
                (assoc sockets _id new-sockets)))))

(defn- get-sockets-updater [modifier]
    (fn [sockets-map {:keys [_id socket]}]
        ((update-fn (cofmap _id socket modifier)) sockets-map)))

(def add-socket (get-sockets-updater conj))
(def remove-socket (get-sockets-updater disj))
;TODO can generalize all of this (along with de-globaling the auth cache)
;into a set of "grouper" or "aggregator" structure manipulators

(def sockets (atom { }))

;honestly not too sure of what to do right now, but there's good
;stuff going on in "state"
(defn socket-handler [request]
    (println request)
    (let [id (get-in request [:route-params :_id])]
        (with-channel request channel
            (let [id-socket-map {:_id id :socket channel}]
                (swap! sockets #(add-socket % id-socket-map))
                (println channel " connected")
                (on-close channel (fn [status]
                    (swap! sockets #(remove-socket % id-socket-map))
                    (println channel " disconnected. status: " status)))))))
