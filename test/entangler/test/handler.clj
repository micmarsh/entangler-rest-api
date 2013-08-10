(ns entangler.test.handler
  (:use clojure.test
        ring.mock.request
        entangler.test.basefns
        entangler.handler)
  (:require [clojure.data.json :as json]))


(def base-info (get-base-info))

(defn param-string [base-info]
  (->> base-info
    (map (fn [[key value]]
      (str \& (name key) \= value)))
    (apply str)))
(defn get-credentials [base-info]
  (dissoc base-info :firstName :lastName))
(defn make-post [route params]
  (request :post route params))
(defn send-post [route params]
    (app (make-post route params)))

(def authtoken (atom nil))
(defn add-auth [request]
  (header request "Authorization" @authtoken))


(deftest login-signup

    (testing "makes a new user"
        (let [response (send-post "/signup?" base-info)]
              (base-check (:body response) base-info)))

    (testing "can't make new user without email"
        (let [response (send-post "/signup?" (dissoc base-info :email))]
              (is (= (:status response) 400))))

    (testing "logs in as just created user"
        (let [just-credentials (get-credentials base-info)
              response (send-post "/login?" just-credentials)
              auth (-> response (get :body) json/read-str (get "authtoken"))]
              (reset! authtoken auth)
              (base-check (:body response) base-info)))

    (testing "can't log in with wrong password"
        (let [just-credentials (get-credentials base-info)
              wrong-password (assoc just-credentials :password "poop")
              response (send-post "/login?" wrong-password)]
              (is (= (:status response) 401))))

    (testing "gets all particles"
      (let [response (app
            (add-auth (request :get "/particles")))]
        (println response)))


)
