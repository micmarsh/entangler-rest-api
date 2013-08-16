(ns entangler.sync
    (:use [org.httpkit.server :only
        [with-channel on-close]]))

(defn add-socket [entangler id socket]
    (assoc entangler id socket))

;could have this close over the atom it's going
;to modify (dependency injection!), which would be better than nothing
;need to read up on managing socket connections/global vars in clojure
(defn socket-handler [request]
    (println request)
    (with-channel request con
    ;(swap! clients assoc con true)
    (println con " connected")
    (on-close con (fn [status]
                    ;(swap! clients dissoc con)
                    (println con " disconnected. status: " status)))))
