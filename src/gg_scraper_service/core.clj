(ns gg-scraper-service.core
  (:require [com.stuartsierra.component :as component]
            [de.otto.tesla.serving-with-httpkit :as httpkit]
            [de.otto.tesla.system :as system]
            [gg-scraper-service.scraper :as scraper])
  (:gen-class))

(defn scraper-service [runtime-config]
  (-> (system/base-system (merge {:name "scraper-service"} runtime-config))
      (assoc :scraper
             (component/using (scraper/new-scraper) [:handler :app-status]))))

(defn -main
  "starts system"
  [& args]
  (system/start (httpkit/add-server (scraper-service {}) :scraper)))
