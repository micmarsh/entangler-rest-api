(ns entangler.test.utils
    (:use clojure.test 
        entangler.utils))


(def entangler-from-kinvey
    (let [without-time (apply merge 
            (for [key [:name :url :_id]]
                {key #(% key)}))]
    (assoc without-time
        :timestamp (fn [kinvey]
                    (let [kmd (kinvey :_kmd)]
                        (:ect kmd))))))

(def kinvey->entangler 
    (get-map-converter entangler-from-kinvey))

(def fake-kinvey 
    {:name "Dinah Moe Hum"
     :url "https://lulz.com"
     :_id 3
     :_kmd {
        :ect "yesterday"
        }})

(deftest map-translator
    (testing "can convert fake kinvey map to entangler map"
        (let [entangler-map (kinvey->entangler fake-kinvey)]
            (is (= (:name entangler-map) (:name fake-kinvey)))
            (is (= "yesterday" (:timestamp entangler-map))))))