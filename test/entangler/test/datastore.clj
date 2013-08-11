(ns entangler.test.datastore
    (:use clojure.test
        entangler.auth
        entangler.datastore
        [kinvey.core :only [kinvey-object?]]))

(def authtoken (atom nil))
(def user-id (atom nil))
(def old-entity (atom nil))
(def created-id (atom nil))

(defn- with-auth [attributes]
    (assoc attributes :authtoken @authtoken))

(def base-attr
    {:url "http://www.homestarrunner.com"
    :name "Homestar Runner"
    :timestamp "2-nite"})

(def other-email "you@you.com")

(def LIMIT 5)

(deftest basic-crud
    (testing "logs in and stores relevant authkey"
        (let [user (login "foo@bar.com" "bar")
             auth (:authtoken user)]
            (is (not (nil? auth)))
            (reset! authtoken auth)
            (reset! user-id (:_id user))))

    (testing "can create things"
        (let [attributes base-attr
              created (create! (with-auth attributes))]
              (is (= attributes (dissoc created :_id :who)))
              (reset! old-entity created)
              (reset! created-id (:_id created))))

    (testing "get one"
        (let [entity (get-one (with-auth {:_id @created-id}))]
          (is (= @old-entity entity))))

    (testing "get some"
        (let [entities (get-many (with-auth {:limit LIMIT}))]
          (is (= (count entities) LIMIT))
          (doseq [entity entities]
            (is (not (kinvey-object? entity))))))

    (testing "update things"
        (let [new-attr {:name "Homestar Runner Dot Net"}
              updated (update! (-> new-attr with-auth (assoc :_id @created-id)))]
              (swap! old-entity #(merge % new-attr))
              (is (= updated @old-entity))))

    (testing "share stuff"
      (let [shared (share! (-> {:email other-email}
                             with-auth
                            (assoc :_id @created-id)))
            whom (:who shared)]
            (is (= (count whom) 2))
            (is (= (merge @old-entity {:who whom}) shared))
            (is (contains? (set whom) @user-id))))

    (testing "delete things"
        (let [deleted (delete! (with-auth {:_id @created-id}) )]
            (is (= deleted {"count" 1}))))
)

;. CUD should be pretty one-to-one,
;get isn't going to have anything but a limit for now,
;share! gets trickier: need to ideally push another user's id into kmd. solution! You'll have id of particle, get
;it as an enitity, do some kind of swap-to-set conj or whatever with the id, then can save it back. Yay
