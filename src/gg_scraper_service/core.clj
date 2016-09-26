(ns gg-scraper-service.core
  (:require [com.stuartsierra.component :as component]
            [de.otto.tesla.serving-with-httpkit :as httpkit]
            [de.otto.tesla.system :as system]
            [gg-scraper-service.grains :as grains])
  (:gen-class))

(defn grains-service [runtime-config]
  (-> (system/base-system (merge {:name "grains-service"} runtime-config))
      (assoc :grains
             (component/using (grains/new-grains) [:handler :app-status]))))

(defn -main
  "starts system"
  [& args]
  (system/start (httpkit/add-server (grains-service {}) :grains)))
