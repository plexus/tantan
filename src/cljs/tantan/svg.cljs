(ns tantan.svg
  (:require [clojure.string :as s]
            [tantan.grid :refer [col-width row-height]]))

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

(defn text [text & {:keys [style]
                    :as props}]
  (let [style-defaults {:text-anchor "middle"
                        :font-family "Sans"}
        lines (s/split text #"\n")]
    [:g
     ;;[:circle {:cx 0 :cy 0 :r 3}]
     (into
      [:text (merge props {:style (merge style-defaults style)}) [:tspan (first lines)]]
      (map (fn [line] [:tspan {:x 0 :dy "1.4em"} line]) (rest lines)))]))
