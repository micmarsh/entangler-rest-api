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


