(ns tantan.grid)

(def row-height 50)
(def col-width  50)

(defn row [i & defs]
  (apply cell 0 i defs))

(defn col [i & defs]
  (apply cell i 0 defs))

(defn cell [x y & defs]
  (into
   [:g {:transform (str "translate(" (* x col-width) " " (* y row-height) ")")}]
   defs))
