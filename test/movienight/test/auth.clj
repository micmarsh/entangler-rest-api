(ns movienight.test.auth
  (:use clojure.test
        movienight.test.random
        movienight.auth))


(def base-info {:email (str (random-str 8) "@molinari.com")
                :password "HRJUkhHYU"
                :firstName "Gustave"
                :lastName "Molinari"})

(defn base-check [user]
    (is (nil? (:error user)))
    (is (= 
        (dissoc base-info :password) 
        (dissoc user :authtoken))))

(deftest signing-up-with-baseinfo 
    (testing "signs up normally"
        (let [signed-up (signup base-info) ]
            (base-check signed-up)))

    (testing "sign up errors"
        (let [error-password (signup (dissoc base-info :password ))
              error-email (signup (dissoc base-info :email))]
              (is (not (nil? (error-email :error))))
              (is (not (nil? (error-password :error))))))

    (testing "regular login"
        (let [{:keys [email password]} base-info
              logged-in (login email password)]
              (base-check logged-in)))

    (testing "two users are in good-auth set"
        (is (= 2 (count @good-auth))))
)