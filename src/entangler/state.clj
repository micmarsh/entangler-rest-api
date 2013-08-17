(ns entangler.state
    (:use [marshmacros.coffee :only [cofmap]]))

(def good-auth (atom #{}))
(def bad-auth (atom #{}))

(defn set-good-auth! [token]
    (swap! bad-auth #(disj % token))
    (swap! good-auth #(conj % token)))
(defn set-bad-auth! [token]
    (swap! good-auth #(disj % token))
    (swap! bad-auth #(conj % token)))

(defn good-auth? [token]
    (@good-auth token))

(defn bad-auth? [token]
    (@bad-auth token))


