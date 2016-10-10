(ns gg-scraper-service.json-server
  (:require     [clojure.data.json :as json]
                [compojure.core :as compojure]
                [compojure.route :as route]
                [com.stuartsierra.component :as c]
                [de.otto.tesla.stateful.handler :as handlers]))

(defrecord JsonServer [handler app-status grains]
  c/Lifecycle
  (start [self]
    (handlers/register-handler
     (:handler self)
     (compojure/routes
      (compojure/GET "/grains" [_]
                     {:status 200
                      :headers {"Access-Control-Allow-Origin" "http://localhost:3000"
                                "Content-Type" "application/json; charset utf-8"}
                      :body (json/write-str grains)})))
    (println "routes added.  Grains are" grains)
    self)
  (stop [self]
    self))

(defn new-json-server [] (map->JsonServer {}))
