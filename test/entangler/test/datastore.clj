(ns entangler.test.datastore
    (:use clojure.test
        entangler.auth
        entangler.datastore
        [kinvey.core :only [kinvey-object?]]))

(def authtoken (atom nil))
(defn- with-auth [attributes]
    (assoc attributes :authtoken @authtoken))
(def created-id (atom nil))
(defn- without-meta [object]
    (dissoc object :_id ))
(def base-attr
    {:url "http://www.homestarrunner.com" 
    :name "Homestar Runner" 
    :timestamp "2-nite"})

(def LIMIT 5)

(def old-entity (atom nil))
(deftest basic-crud 
    (testing "logs in and stores relevant authkey"
        (let [user (login "foo@bar.com" "bar")
             auth (:authtoken user)]
            (is (not (nil? auth)))
            (reset! authtoken auth)))

    (testing "can create things"
        (let [attributes base-attr
              created (create! (with-auth attributes))]
              (is (= attributes (without-meta created)))
              (reset! old-entity created)
              (reset! created-id (:_id created))))

    (testing "get one"
        (let [entity (get-one (with-auth {:_id @created-id}))]
          (is (= @old-entity entity))))

    (testing "get some"
        (let [entities (get-many (with-auth {:limit LIMIT}))]
          (is (= (count entities) LIMIT))
          (doseq [entity entities]
            (is (not (kinvey-object? entity)))
            )))

    (testing "update things"
        (let [new-attr {:name "Homestar Runner Dot Net"}
              updated (update! (-> new-attr with-auth (assoc :_id @created-id)))
              stripped (without-meta updated)]
              (is (= stripped (merge stripped new-attr)))))
          
    (testing "delete things"
        (let [deleted (delete! (with-auth {:_id @created-id}) )]
            (is (= deleted {"count" 1}))))

)

;. CUD should be pretty one-to-one, 
;get isn't going to have anything but a limit for now,
;share! gets trickier: need to ideally push another user's id into kmd. solution! You'll have id of particle, get
;it as an enitity, do some kind of swap-to-set conj or whatever with the id, then can save it back. Yay