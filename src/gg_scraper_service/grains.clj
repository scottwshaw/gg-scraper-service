(ns gg-scraper-service.grains
  (:require     [compojure.core :as compojure]
                [compojure.route :as route]
                [com.stuartsierra.component :as c]
                [de.otto.tesla.stateful.handler :as handlers]
                [hiccup.page :as page]
                [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* "http://www.grainandgrape.com.au")
(def ^:dynamic *init-grain-path* "/products/category/NFBWLKNJ-grain-malted-and-unmalted")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn next-link? [n]
  (clojure.string/includes? (-> n :content first) "Next" ))

(defn fetch-grain-page [page-path]
  (let [response (fetch-url (str *base-url* page-path))
        nav-nodes (filter next-link? (html/select response [:a.prev_next]))]
    {:page-of-grains (map #(-> % html/text clojure.string/trim) (html/select response [:div.description]))
     :next (-> nav-nodes first :attrs :href)}))

(defn fetch-all-grains []
  (loop [path *init-grain-path* grain-list []]
    (let [{:keys [page-of-grains next]} (fetch-grain-page path)
          all-grains (concat grain-list page-of-grains)]
      (if (nil? next)
        all-grains
        (recur next all-grains)))))

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

(defrecord Grains [handler app-status]
  c/Lifecycle
  (start [self]
    (handlers/register-handler
     (:handler self)
     (compojure/routes (compojure/GET "/grains" [_]
                                      (basic-template "grains" (grainslist (fetch-all-grains))))))f
    self)
 (stop [self]
   self))

(defn new-grains [] (map->Grains {}))
