(ns entangler.sync
    (:use [org.httpkit.server :only
        [with-channel on-close]]))

(defn add-socket [entangler id socket]
    (assoc entangler id socket))

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
