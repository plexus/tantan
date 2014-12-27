(ns tantan.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as s]
            [tantan.grid :as grid]
            [tantan.traits :refer [render-entry]]))

(def app-state (atom {:parts [{:text "緊"
                               :color "blue"
                               :pinyin "jin3"
                               :traits #{:text :color :pinyin}}
                              {:text "張"
                               :color "red"
                               :pinyin "zhang1"
                               :traits #{:text :color}}]
                      :children [{:text "緊"
                                  :traits #{:text}}
                                 {:text "張"
                                  :children [{:text "弓"
                                              :traits #{:text}}
                                             {:text "長"
                                              :traits #{:text}}]
                                  :traits #{:text :children}}]
                      :traits #{:children :parts}}))

(defn app [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:svg {:width 500 :height 500}
             (grid/cell 4 2 (render-entry app))]))))

(defn main []
  (om/root app app-state {:target (. js/document (getElementById "app"))}))
