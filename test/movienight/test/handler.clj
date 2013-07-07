(ns movienight.test.handler
  (:use clojure.test
        ring.mock.request
        movienight.handler))



(deftest test-app
  (def posted-id (atom nil))

  (testing "gets all videos"
    (let [response (app (request :get "/videos"))]
      (is (= (:status response) 200))
      (is (= (:body response) ALL_MESSAGE))))

  (testing "adds video and gets its id"
    (let [response (app (request :post (str "/videos?url=" FAKE_URL) ))
          body (:body response)
          id (:id body)]
          (is (not (nil? body)))
          (is (not (nil? id)))
          (reset! posted-id id)))

  (testing "gets video with id saved id above"
    (let [video-route (str "/videos/" @posted-id)
          response (app (request :get video-route))
          body (:body response)]
      (is (= (:id body) @posted-id))
      (is (= (:url body) FAKE_URL))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))