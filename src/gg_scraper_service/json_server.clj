(ns gg-scraper-service.json-server
  (:require     [cheshire.core :as ch]
                [compojure.core :as compojure]
                [compojure.route :as route]
                [com.stuartsierra.component :as c]
                [de.otto.tesla.stateful.handler :as handlers]))

(defrecord JsonServer [handler app-status grains]
  c/Lifecycle
  (start [self]
    (handlers/register-handler
     (:handler self)
     (compojure/routes (compojure/GET "/grains" [_]
                                      {:http-equiv "Content-Type" :content "application/json" :charset "utf-8"}
                                      (ch/generate-string (:data grains)))))
    (println "routes added.  Grains are" grains)
    self)
  (stop [self]
    self))

(defn new-json-server [] (map->JsonServer {}))
