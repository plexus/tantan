(ns tantan.svg
  (:require [clojure.string :as s]))

(defn path [& {:keys [d stroke fill]
               :or {stroke "black"
                    fill "transparent"}
               :as props}]
  (letfn [(stringify [s]
            (if (keyword? s)
              (name s)
              s))]
    [:path (merge props {:d (s/join " " (map stringify d))
                         :stroke stroke
                         :fill fill})]))
