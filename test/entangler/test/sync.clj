(ns entangler.test.sync
    (:use
        clojure.test
        [entangler.state :only [sockets add-socket!]]
        [marshmacros.coffee :only [cofmap]]))

(def LULZ_ID "lulzlulzlulz")
(def SOCKET {:connection "the best"})
(def RANGE (range 3))
(defn range-vector [function]
    (vec (for [i RANGE]
        (function i))))

(deftest sockets-state
    (testing "can add multiple sockets to the map socket map"
        (let [fake-ids (range-vector #(str LULZ_ID %))
              fake-sockets (range-vector #(assoc SOCKET :id %)) ]
              (doseq [i RANGE]
                (add-socket! {:_id (fake-ids i) :socket (fake-sockets i)}))
              (is (= (count @sockets) (count RANGE)))))

    (testing "sockets are associated to entity ID's correctly"
        (let [socket-sets (range-vector #(@sockets (str LULZ_ID %))) ]
            (doseq [i RANGE]
                (let [socket-set (socket-sets i)]
                    (is (= (count socket-set) 1))
                    (is (contains? socket-set (assoc SOCKET :id i)))))))

)


