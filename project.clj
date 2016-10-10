(defproject gg-scraper-service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.stuartsierra/component "0.3.1"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [de.otto/tesla-httpkit "0.1.5"]
                 [de.otto/tesla-microservice "0.3.33"]
                 [enlive "1.1.6"]
                 [hiccup "1.0.5"]]
  :main ^:skip-aot gg-scraper-service.core)
