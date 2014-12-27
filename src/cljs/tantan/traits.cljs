(ns tantan.traits
  (:require [tantan.svg :as svg]
            [tantan.grid :as grid]))

(declare render-children)
(declare render-entry)

(defn text [entry props]
  (conj entry [:text {:x 0 :y 43 :style {:font-size "50px" :text-anchor "middle"}}
               (:text props)]))

(defn parts [entry data]
  (let [parts (:parts data)
        offset (* 0.5 (dec (count parts)))]
    (into entry
          (map #(grid/col (- %1 offset) %2)
               (range)
               (map render-entry parts)))))

(defn color [entry data]
  [:g {:style {:fill (:color data)}} entry])

(defn pinyin [entry data]
  [:g {:transform "scale(0.8)"}
   entry
   [:text {:x 0 :y 65 :style {:text-anchor "middle" :font-family "Sans"}} (:pinyin data)]])

(defn children [entry data]
  (conj entry (grid/row 1 (render-children (:children data)))))

(def all-traits [:parts parts
                 :text  text
                 :pinyin pinyin
                 :color color
                 :children children])

(defn apply-traits [traits props entry]
  (reduce (fn [entry [tname tfn]]
            (if (contains? traits tname)
              (tfn entry props)
              entry)) entry (partition 2 all-traits)))

(defn render-entry [data]
  (let [traits (or (:traits data) '())]
    (apply-traits traits data [:g])))

(defn render-edge [idx]
  (svg/path :d [:M 0 0 :C (* idx 25) 0, (* idx 50) 25, (* idx 50) 45]
            :stroke-width "2"))

(defn render-children [children]
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
              (map #(grid/col %2 (render-entry %1)) children edge-range))]
      [:g])))
