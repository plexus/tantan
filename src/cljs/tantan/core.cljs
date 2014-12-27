(ns tantan.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as s]
            [tantan.svg :as svg]
            [tantan.grid :as grid]))

(def app-state (atom {:props {:text "緊張"}
                      :edges [{:props {:text "緊"}}
                              {:props {:text "張"}
                               :edges [{:props {:text "弓"}}
                                       {:props {:text "長"}}]}]}))

(defn render-edge [idx]
  (svg/path :d [:M 0 0 :C (* idx 25) 0, (* idx 50) 25, (* idx 50) 45]
            :stroke-width "2"))

(defn render-edges [edges]
  (let [count (count edges)
        even (even? count)
        edge-range (range (- count) count)
        edge-range (filter (if even odd? even?) edge-range)]
    (if (> count 0)
      [:g
       (svg/path :d [:M 0 5 :V 50] :stroke-width "2")
       (apply grid/row 1
              (map render-edge edge-range))
       (apply grid/row 2
              (map #(grid/col %2 (render-entry %1)) edges edge-range))]
      [:g])))

(defn render-entry [data]
  (let [props (:props data)
        edges (:edges data)]
    [:g
     ;; [:circle {:fill "red" :cx 0 :cy 0 :r 3}]
     ;; [:circle {:fill "blue" :cx 50 :cy 50 :r 3}]
     [:text {:x 0 :y 43 :style {:font-size "50px" :text-anchor "middle"}} (:text props)]
     (grid/cell 0 1
           (render-edges edges))]))


(defn app [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:svg {:width 500 :height 500}
             (grid/cell 4 2
                (render-entry app))]))))

(defn main []
  (om/root app app-state {:target (. js/document (getElementById "app"))}))
