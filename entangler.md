# What to do

The primary job of entangler is to accept a message and relay it. This means it receives and holds open a bunch of socket and/or long poll conections (sockets), each of which is associated with an id, and each incoming message has an id

Higher level: relay changes in an object's state. Since pretending like you actually have objects like that is dumb for rest apis, it's better to wrap update in another function that concurrently updates and sends a message to entangler. It definitely sounds like entangler is somehting like a module with all kinds of state bound up inside it, best too look into ways to avoid that.

found a way!


(defn wrap-updater [func, ent-serv]
    (fn [update-args]
        (send args to ent-serv)
        (func args)))

(:use [entangler :only [server wrap-updater]])

(def entangler-server (server)) ;or something

(def update! (wrap-updater actual-update-fn entangler-server))
;or something

Now need a way to hook sockets up to ent serv. Look up how to actually socket in Ring, read up on ring wrappers, other functional concepts like above, go crazy
