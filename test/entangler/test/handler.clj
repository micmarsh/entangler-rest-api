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

(defn get-body [response]
  (-> response :body json/read-str))

(def authtoken (atom nil))

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


(defn add-auth [request]
  (header request "Authorization" @authtoken))

(def created-items (atom []))

(defn check-all
  ([body]
    (check-all body nil))
  ([body new-name]
    (doseq [entity body]
      (let [{:keys [url name timestamp _id]} (kewordize entity) ]
        (is (not (nil? url)))
        (is (not (nil? timestamp)))
        (is (contains? (set @created-items) _id))
        (is (= new-name name))))))

(defn id-comprehension
  ([ids]
    (id-comprehension ids :get))
  ([ids, method]
    (id-comprehension ids method {}))
  ([ids, method, params]
    (for [id ids]
      (let [authed-request (add-auth
              (request method (str "/particles/" id) params))
            response (app authed-request)]
            (get-body response)))))

(def NAME "Monkeytown")

(deftest CRUD

    (testing "can make three new entities"
      (doseq [text ["godspeed" "pretentiousfriends" "nightcreatures"]]
        (let [params {:url (str text ".com") :timestamp "Tomorrow Night"}
              request (make-post "/particles" params)
              authed (add-auth request)
              response (app authed)
              body (get-body response)]
              (is (= (body "url") (str text ".com")))
              (is (= (body "timestamp") "Tomorrow Night"))
              (swap! created-items #(conj % (body "_id"))))))

    (testing "gets each particle"
      (let [particles (id-comprehension @created-items)]
        (check-all particles)))

    (testing "gives each particle a name"
      (let [updated-particles (id-comprehension
              @created-items :put {:name NAME})]
          (check-all updated-particles NAME)))

    (testing "gets all particles"
      (let [response (app
            (add-auth (request :get "/particles")))
            body (get-body response)]
          (is (= (count body) 3))
          (check-all body NAME)))

    (testing "shares a particle"
      (let [other-id "othersID123"
            to-send (request :put
              (str "/particles/" (@created-items 0))
              {:shareto other-id})
            response (app (add-auth to-send))
            body (get-body response)
            who (set (body "who"))]
            (is (= (count who) 2))
            (is (contains? who other-id))))

    (testing "deletes all particles"
      (let [delete-responses (id-comprehension
              @created-items :delete )
            n (println delete-responses)
            no-more-entities (app
              (add-auth (request :get "/particles")))
            body (get-body no-more-entities)]
            (is (= (count body) 0))))
)
