(ns tantan.traits
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [om.core :as om :include-macros true]
            [tantan.svg :as svg]
            [tantan.grid :as grid :refer [col-width row-height]]))

(declare render-children)
(declare render-entry)

(defn find-col-width [element]
  (if-let [cols (:cols (get element 1))]
    cols
    (if-let [cs (next (next element))]
      (reduce + (map find-col-width cs))
      0)))

(defn char [entry data owner]
  [:g {:width col-width}
   entry
   (svg/text (:text data) :cols 1 :x 0 :y 43
             :style {:font-size (str col-width "px")})])

(defn parts [entry data owner]
  (let [parts (:parts data)
        entries (map render-entry parts (repeat owner))
        col-widths (map find-col-width entries)
        cols (reduce + col-widths)
        offset (* 0.5 (dec cols))]
    (into entry
          (map #(grid/col (- %1 offset) %2)
               (reductions + 0 col-widths)
               entries))))

(defn color [entry data owner]
  [:g {:style {:fill (:color data)}} entry])

(defn pinyin [entry data owner]
  [:g {:transform "scale(0.8)"}
   entry
   (svg/text (:pinyin data) :x 0 :y 65)])

(defn hovered [entry data owner]
  [:g
   [:rect {:x -25 :y 0 :width grid/col-width :height grid/row-height :style {:fill "#eeffaa"}}]
   entry])

(defn children [entry data owner]
  [:g entry (grid/row 1 (render-children (:children data) owner))])

(defn interactive [entry data owner]
  [:g
   {:on-mouse-enter #(put! (:event-chan (om/get-shared owner)) [:mouse-enter data])
    :on-mouse-leave #(put! (:event-chan (om/get-shared owner)) [:mouse-leave data])
    :on-click       #(put! (:event-chan (om/get-shared owner)) [:click data])}
   entry])

(defn text [entry data owner]
  [:g {:width col-width}
   entry
   (svg/text (:text data) :cols 4 :y "1em" :style {:text-anchor "left"})])

(def all-traits [:parts parts
                 :char char
                 :text text
                 :pinyin pinyin
                 :interactive interactive
                 :color color
                 :hovered hovered
                 :children children])

(defn apply-traits [data owner entry]
  (let [traits (or (:traits data) '())]
    (reduce (fn [entry [tname tfn]]
              (if (contains? traits tname)
                (tfn entry data owner)
                entry)) entry (partition 2 all-traits))))

(defn render-entry [cursor owner]
  (apply-traits cursor
                owner
                [:g]))

(defn render-edge [idx]
  (svg/path :d [:M 0 0 :C (* idx 25) 0, (* idx 50) 25, (* idx 50) 45]
            :stroke-width "2"))

(defn render-children [children owner]
  (let [count (count children)
        even (even? count)
        edge-range (range (- count) count)
        edge-range (filter (if even odd? even?) edge-range)]
    (if (> count 0)
      [:g
       (svg/path :d [:M 0 5 :V 50] :stroke-width "2")
       (apply grid/row 1
              (map render-edge edge-range))
       (apply grid/row 2
              (map #(grid/col %2 (render-entry %1 %3)) children edge-range (repeat owner)))]
      [:g])))
