(ns gg-scraper-service.retry
  (:import [clojure.core.async.impl.channels ManyToManyChannel])
  (:require [clojure.core.async :as async]))

;;;
;;; From https://gist.github.com/jcf/18b9d64b7ae7b4f45ba7
;;;

(defn retry
  [f & {:keys [out-ch retries init-delay message]}]
  {:pre [(and (integer? retries) (pos? retries))
         (integer? init-delay)
         (instance? ManyToManyChannel out-ch)]}
  (let [stop-ch (async/chan 1)]
    (async/go-loop [n 0
                    delay init-delay]
      (when (< n retries)
        (let [result (async/<! (async/thread
                                 (try
                                   (f)
                                   (catch Exception ex
                                     {::fail ex}))))]
          (if (::fail result)
            (do
              (println "retrying: " message)
              (async/alt!
                (async/timeout delay) (recur (inc n) (* 2 delay)) ;double delay every time
                stop-ch :stopped))
            (do
              (if (some? result) (async/>! out-ch result))
              (async/close! stop-ch)
              (async/close! out-ch))))))
    stop-ch))
