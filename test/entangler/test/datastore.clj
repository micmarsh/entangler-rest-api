(ns entangler.test.datastore
    (:use clojure.test
        entangler.test.random
        entangler.auth))

(def authtoken (atom nil))
(defn- with-auth [attributes]
    (assoc attributes :authtoken @authtoken))
(def created-id (atom nil))
(defn- without-meta [object]
    (dissoc object :_id :timestamp))
(def base-attr
    {:url "http://www.homestarrunner.com" :name "Homestar Runner"})


(deftest basic-crud 
    (testing "logs in and stores relevant authkey"
        (let [user (login "foo@bar.com" "bar")
             auth (:authtoken user)]
            (is (not (nil? auth)))
            (reset! authtoken auth))
    (testing "can create things"
        (let [attributes base-attr
              created (create! (with-auth attributes))]
              (is (= created (without-meta attributes)))
              (reset! created-id (:_id attributes))))
    (testing "update things"
        (let [new-attr {:name "Homestar Runner Dot Net"}
              updated (update! (-> new-attr with-auth (assoc :_id @created-id)))
              stripped (without-meta updated)]
              (is (= stripped (assoc stripped new-attr)))))
    (testing "delete things"
        (let [deleted (delete! {:_id @created-id})]
            (is (= deleted {:count 1}))))

)

;. CUD should be pretty one-to-one, 
;get isn't going to have anything but a limit for now,
;share! gets trickier: need to ideally push another user's id into kmd. solution! You'll have id of particle, get
;it as an enitity, do some kind of swap-to-set conj or whatever with the id, then can save it back. Yay