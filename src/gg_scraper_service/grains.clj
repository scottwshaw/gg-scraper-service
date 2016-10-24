(ns gg-scraper-service.grains
  (:require     [clojure.core.async :as async]
                [compojure.core :as compojure]
                [compojure.route :as route]
                [com.stuartsierra.component :as c]
                [de.otto.tesla.stateful.handler :as handlers]
                [gg-scraper-service.retry :as r]
                [hiccup.page :as page]
                [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* "http://www.grainandgrape.com.au")
(def ^:dynamic *init-specialty-grain-path* "/products/category/HOPLAJMQ-specialty")
(def ^:dynamic *init-base-grain-path* "/products/category/DEOLHJTI-base-malt")
(def max-backoff 15)
(def backoff-increment 5)

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn next-link? [n]
  (clojure.string/includes? (-> n :content first) "Next" ))

(defn fetch-url-with-retry [url]
  (let [nretries 5
        delay 5000
        response-ch (async/chan 1)]
    (do
      (r/retry #(fetch-url url)
               :out-ch response-ch
               :retries nretries
               :init-delay delay
               :message (str "connection to " url))
      (async/<!! response-ch))))


(defn fetch-grain-page [page-path]
  (let [response (fetch-url-with-retry (str *base-url* page-path))
        nav-nodes (filter next-link? (html/select response [:a.prev_next]))]
    {:page-of-grains (map #(-> % html/text clojure.string/trim) (html/select response [:div.description]))
     :next (-> nav-nodes first :attrs :href)}))

(defn fetch-all-grain-pages-from [init-grain-path]
  (loop [path init-grain-path grain-list []]
    (let [{:keys [page-of-grains next]} (fetch-grain-page path)
          all-grains (concat grain-list page-of-grains)]
      (if (nil? next)
        (do
          (println all-grains)
          all-grains)
        (recur next all-grains)))))

(defn fetch-all-grains []
  (concat
   (fetch-all-grain-pages-from *init-base-grain-path*)
   (fetch-all-grain-pages-from *init-specialty-grain-path*)))

(defrecord GrainList []
  c/Lifecycle
  (start [self]
    (assoc self :data (fetch-all-grains)))
  (stop [self]
    (assoc self :data nil)))

(defn new-grain-list [] (map->GrainList {}))
