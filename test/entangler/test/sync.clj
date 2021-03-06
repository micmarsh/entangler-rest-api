(ns entangler.test.sync
    (:use
        clojure.test
        [entangler.sync :only [add-socket remove-socket]]
        [marshmacros.coffee :only [cofmap]]))

(def LULZ_ID "lulzlulzlulz")
(def SOCKET {:connection "the best"})

(def RANGE (range 3))
(defn range-vector [function]
    (vec (for [i RANGE]
        (function i))))

(def FAKE_SOCKETS (range-vector #(assoc SOCKET :id %)))
(def FAKE_IDS (range-vector #(str LULZ_ID %)))

(defn get-socket-sets [sockets]
    (range-vector #(sockets (str LULZ_ID %))))

(def sockets-map  (atom {}))

(deftest sockets-state
    (testing "can add multiple sockets to the map socket map"
        (doseq [i RANGE]
            (swap! sockets-map
                #(add-socket % {:_id (FAKE_IDS i) :socket (FAKE_SOCKETS i)})))
        (is (= (count @sockets-map) (count RANGE))))

    (testing "sockets are associated to entity ID's correctly"
        (let [socket-sets (range-vector #(@sockets-map (str LULZ_ID %))) ]
            (doseq [i RANGE]
                (let [socket-set (socket-sets i)]
                    (is (= (count socket-set) 1))
                    (is (contains? socket-set (assoc SOCKET :id i)))))))

    (testing "delete those sockets"
        (doseq [i RANGE]
            (swap! sockets-map
                #(remove-socket % {:_id (FAKE_IDS i) :socket (FAKE_SOCKETS i)})))
        (is (= (count @sockets-map) 0)))

)


