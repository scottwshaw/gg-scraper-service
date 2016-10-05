(ns gg-scraper-service.core
  (:require [com.stuartsierra.component :as component]
            [de.otto.tesla.serving-with-httpkit :as httpkit]
            [de.otto.tesla.system :as system]
            [gg-scraper-service.grains :as grains]
            [gg-scraper-service.json-server :as js])
  (:gen-class))

(defn grains-service [runtime-config]
  (-> (system/base-system (merge {:name "grains-service"} runtime-config))
      (assoc :grains (grains/new-grain-list))
      (assoc :grain-server
             (component/using (js/new-json-server) [:handler :app-status :grains]))))

(defn -main
  "starts system"
  [& args]
  (system/start (httpkit/add-server (grains-service {}) :grain-server)))
