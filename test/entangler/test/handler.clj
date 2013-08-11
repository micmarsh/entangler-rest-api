(ns entangler.test.handler
  (:use clojure.test
        ring.mock.request
        entangler.test.basefns
        entangler.handler)
  (:require [clojure.data.json :as json]))


(defn kewordize [hashmap]
  (apply merge (map (fn [[key value]]
    {(keyword key) value})
      hashmap)))

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

(def created-items (atom []))

(defn check-all [body]
    (doseq [entity body]
      (let [{:keys [url name timestamp _id]} (kewordize entity) ]
        (is (not (nil? url)))
        (is (not (nil? timestamp)))
        (is (contains? (set @created-items) _id))
        (is (nil? name)))))


(deftest login-signup

    (testing "makes a new user"
        (let [response (send-post "/signup?" base-info)]
              (base-check (:body response) base-info)))

    (testing "can't make new user without email"
        (let [response (send-post "/signup" (dissoc base-info :email))]
              (is (= (:status response) 400))))

    (testing "logs in as just created user"
        (let [just-credentials (get-credentials base-info)
              response (send-post "/login" just-credentials)
              auth (-> response (get :body) json/read-str (get "authtoken"))]
              (reset! authtoken auth)
              (base-check (:body response) base-info)))

    (testing "can't log in with wrong password"
        (let [just-credentials (get-credentials base-info)
              wrong-password (assoc just-credentials :password "poop")
              response (send-post "/login" wrong-password)]
              (is (= (:status response) 401))))
)

(deftest CRUD

    (testing "can make three new entities"
      (doseq [text ["godspeed" "pretentiousfriends" "nightcreatures"]]
        (let [params {:url (str text ".com") :timestamp "Tomorrow Night"}
              request (make-post "/particles" params)
              authed (add-auth request)
              response (app authed)
              body (-> response :body json/read-str)]
              (is (= (body "url") (str text ".com")))
              (is (= (body "timestamp") "Tomorrow Night"))
              (swap! created-items #(conj % (body "_id"))))))


    (testing "gets each particle"
      (check-all (for [id @created-items]
        (let [authed-request (add-auth
                (request :get (str "/particles/" id)))
              response (app authed-request)]
              (-> response :body json/read-str)))))

    (testing "gets all particles"
      (let [response (app
            (add-auth (request :get "/particles")))
            body (-> response :body json/read-str)]
          (is (= (count body) 3))
          (check-all body)))
)
