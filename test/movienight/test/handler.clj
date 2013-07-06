(ns movienight.test.handler
  (:use clojure.test
        ring.mock.request
        movienight.handler))

(deftest test-app
  (testing "gets all videos"
    (let [response (app (request :get "/videos"))]
      (is (= (:status response) 200))
      (is (= (:body response) ALL_MESSAGE))))
  (testing "gets video with id 'abc'"
    (let [response (app (request :get "/videos/abc"))
          body (:body response)]
      (is (= (:id body) "abc"))
      (is (= (:url body) FAKE_URL))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))