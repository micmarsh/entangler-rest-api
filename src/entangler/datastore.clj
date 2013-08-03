(ns entangler.datastore 
    (:require [entangler.auth :as auth]
              [kinvey.core :as k]))

(defn- get-collection [token]
    (-> auth/kinvey-app
        (k/load-user token)
        (k/make-collection "things")))

;TODO: this is basically a super-straightforward interface to kinvey.
;the fun stuff is going to be generalizing a the video conversion framework and syncing
;actually, just do syncing since you can ffmpeg shit locally if you really need to
;ideas: look around for clojure based ideas first, but will for sure need
 
(defn create! [params]
    ())

