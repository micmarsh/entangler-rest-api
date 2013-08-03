(defproject entangler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [liberator "0.9.0"]
                 [compojure "1.1.5"]
                 [marshmacros "0.2.1"]
                 [crypto-random "1.1.0"]
                 [kinvey "0.1.2"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler entangler.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
