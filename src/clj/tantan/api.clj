(ns tantan.api
  (:require [tantan.analects :as analects :refer :all]
            [ring.util.response :refer [response]]
            [clojure.string :as s]))

(defn chise-entry [entry]
  (let [parts (map str (chise-ids-decomp entry))
        children? (not (empty? parts))]
    {
     :text entry
     :children (if children? (mapv chise-entry parts) [])
     :traits (cond-> #{:char}
               children? (conj :children))}))

(defn cedict-entry
  ([char pinyin]
   (let [parts (map str (chise-ids-decomp char))
         children? (not (empty? parts))]
     {:text char
      :pinyin pinyin
      :children (if children? (mapv chise-entry parts) [])
      :traits (cond-> #{:char :pinyin}
                children? (conj :children))}))
  ([entry]
   (let [cedicts (->> entry
                      (cedict-lookup-zh)
                      (map (fn [[_ char _ pinyin]] [char pinyin]))
                      (distinct))
         multi-pinyin? (> (count cedicts) 1)]
     (if multi-pinyin?
       {:parts (mapv (fn [char] {:text char :traits #{:char}}) entry)
        :children (mapv (fn [[char pinyin]] (cedict-entry char pinyin)) cedicts)
        :traits #{:parts :children}}
       (if (> (count entry) 1)
         (let [[chars pinyin] (first cedicts)]
           {:parts (mapv (fn [char p] {:text char :pinyin p :traits #{:char :pinyin}}) chars (s/split pinyin #" "))
            :children (mapv chise-entry (map str (seq chars)))
            :traits #{:parts :children}})
         (merge (chise-entry entry)
                {:parts (mapv (fn [char] {:text char :traits #{:char}}) entry)
                 :traits #{:parts :children}}))))))

(defn find-entry [entry]
  (response (cedict-entry entry))
  #_(response {:parts [{:text "緊"
                      :color "blue"
                      :pinyin "jin3"
                      :traits #{:char :color}}
                     {:text "張"
                      :color "red"
                      :pinyin "zhang1"
                      :traits #{:char :color :pinyin}}]
             :children [{:parts [{:text "緊"
                                  :traits #{:char :interactive}}]
                         :traits #{:parts}}
                        {:text "張"
                         :children [{:text "弓"
                                     :children [{:text "引"
                                                 :traits #{:char :interactive}}
                                                {:text "弹"
                                                 :traits #{:char :interactive}}
                                                {:text "粥"
                                                 :traits #{:char :interactive}}
                                                {:text "彊"
                                                 :traits #{:char :interactive}}
                                                {:text "彎"
                                                 :traits #{:char :interactive}}
                                                ]
                                     :traits #{:children :char :interactive}}
                                    {:text "長"
                                     :traits #{:char :interactive}}]
                         :traits #{:char :interactive :children}}]
             :traits #{:children :parts :interactive}}))
