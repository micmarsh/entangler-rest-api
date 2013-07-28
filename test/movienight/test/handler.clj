(ns movienight.test.handler
  (:use clojure.test
        ring.mock.request
        movienight.test.basefns
        movienight.handler))


(def base-info (get-base-info))

(defn param-string [base-info]
  (->> base-info
    (map (fn [[key value]]
      (str \& (name key) \= value)))
    (apply str)))
(defn get-credentials [base-info]
  (dissoc base-info :firstName :lastName))
(defn make-post [route params]
    (app (request :post 
        (str route (param-string params)))))

(deftest login-signup

    (testing "makes a new user"
        (let [response (make-post "/signup?" base-info)]
              (base-check (:body response) base-info)))

    (testing "can't make new user without email"
        (let [response (make-post "/signup?" (dissoc base-info :email))]
              (is (= (:status response) 401))))

    (testing "logs in as just created user"
        (let [just-credentials (get-credentials base-info)
              response (make-post "/login?" just-credentials)]
              (base-check (:body response) base-info)))

    ; (testing "can't log in with wrong password"
    ;     (let [just-credentials (get-credentials base-info)
    ;           wrong-password (assoc just-credentials :password "poop")
    ;           response (make-post "/login?" wrong-password)]
    ;           (is (= (:status response) 401))))

  ; (testing "gets all videos"
  ;   (let [response (app (request :get "/videos"))]
  ;     (is (= (:status response) 200))
  ;     (is (= (:body response) ALL_MESSAGE))))

  ; (testing "adds video and gets its id"
  ;   (let [response (app (request :post (str "/videos?url=" FAKE_URL) ))
  ;         body (:body response)
  ;         id (:id body)]
  ;         (is (not (nil? body)))
  ;         (is (not (nil? id)))
  ;         (reset! posted-id id)))

  ; (testing "gets video with id saved id above"
  ;   (let [video-route (str "/videos/" @posted-id)
  ;         response (app (request :get video-route))
  ;         body (:body response)]
  ;     (is (= (:id body) @posted-id))
  ;     (is (= (:url body) FAKE_URL))))

  ; (testing "doesn't get a video that isn't there"
  ;   (let [video-route "/videos/meet_lol"
  ;         response (app (request :get video-route))
  ;         {:keys [body]} response]))

  ; (testing "not-found route"
  ;   (let [response (app (request :get "/invalid"))]
  ;     (is (= (:status response) 404))))

)