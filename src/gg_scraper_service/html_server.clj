(ns gg-scraper-service.html-server
  (:require     [compojure.core :as compojure]
                [compojure.route :as route]
                [com.stuartsierra.component :as c]
                [de.otto.tesla.stateful.handler :as handlers]
                [hiccup.page :as page]
                [net.cgrand.enlive-html :as html]))

(defn head [title]
  [:head
   [:title title]
   [:meta {:http-equiv "Content-Type" :content "text/html" :charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (page/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")])

(defn basic-template [title content]
  (page/html5 {:lang "en"}
              (head (str title))
              [:body
               [:div {:class "container"}
                [:div {:class "jumbotron"} content]]]))

(defn grainslist [grains]
  [:div [:ol (for [i grains] [:li i])]])

(defrecord HtmlServer [handler app-status grains]
  c/Lifecycle
  (start [self]
    (handlers/register-handler
     (:handler self)
     (compojure/routes (compojure/GET "/grains" [_]
                                      (basic-template "grains" (grainslist (:data grains))))))
    (println "routes added")
    self)
  (stop [self]
    self))

(defn new-html-server [] (map->HtmlServer {}))
