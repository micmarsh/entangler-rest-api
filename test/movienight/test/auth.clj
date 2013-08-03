(ns movienight.test.auth
  (:use clojure.test
        movienight.test.random
        movienight.test.basefns
        movienight.auth))


(def base-info (get-base-info))
(def logged-in-user (atom nil))

(deftest signing-up-with-baseinfo 
    (testing "signs up normally"
        (let [signed-up (signup base-info) ]
            (base-check signed-up base-info)))

    (testing "sign up errors"
        (let [error-password (signup (dissoc base-info :password ))
              error-email (signup (dissoc base-info :email))]
              (is (not (nil? (error-email :error))))
              (is (not (nil? (error-password :error))))))

    (testing "regular login"
        (let [{:keys [email password]} base-info
              logged-in (login email password)]
              (base-check logged-in base-info)
              (reset! logged-in-user logged-in)))

    (testing "two users are in good-auth set"
        (is (= 2 (count @good-auth))))

    (testing "logged-in-user is authorized"
      (let [authed (authorized? (:authtoken @logged-in-user))]
        (println authed)))
)