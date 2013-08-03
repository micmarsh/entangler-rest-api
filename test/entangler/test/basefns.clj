(ns entangler.test.basefns
    (:use 
        clojure.test
        entangler.test.random)
     (:require [clojure.data.json :as json]))

(defn get-base-info []
               {:email (str (random-str 8) "@molinari.com")
                :password "HRJUkhHYU"
                :firstName "Gustave"
                :lastName "Molinari"})

(defn- keywords->strings [keyword-map]
    (into { } (for [[k v] keyword-map]
        [(name k) v])))
(defn- strings->keywords [string-map]
    (into { } (for [[k v] string-map]
        [(keyword k) v])))

(defn- convert-first-arg [function]
    (fn [user, base]
        (let [json-user
            (if (= (type user) java.lang.String)
            (strings->keywords (json/read-str user))
            user)]
        (function json-user base))))


(defn- check [user, base]
    (is (nil? (:error user)))
    (is (= 
        (dissoc base :password ) 
        (dissoc user :authtoken :_id))))

(def base-check (convert-first-arg check))
