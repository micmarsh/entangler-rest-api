(ns entangler.sync
    (:use [org.httpkit.server :only
        [with-channel on-close]]
        [marshmacros.coffee :only [cofmap]]))

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
    (fn [sockets-map {:keys [_id socket]}]
        ((update-fn (cofmap _id socket modifier)) sockets-map)))

(def add-socket (get-sockets-updater conj))
(def remove-socket (get-sockets-updater disj))


;honestly not too sure of what to do right now, but there's good
;stuff going on in "state"
(defn socket-handler [request]
    (println request)
    (with-channel request con
    ;(swap! clients assoc con true)
    (println con " connected")
    (on-close con (fn [status]
                    ;(swap! clients dissoc con)
                    (println con " disconnected. status: " status)))))
