(ns tantan.core
  (:require [cljs.core.async :as async :refer [>! <! chan put! close!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as s]
            [tantan.grid :as grid]
            [tantan.traits :refer [render-entry]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def app-state (atom {:parts [{:text "緊"
                               :color "blue"
                               :pinyin "jin3"
                               :traits #{:char :color :pinyin}}
                              {:text "張"
                               :color "red"
                               :pinyin "zhang1"
                               :traits #{:char :color}}]
                      :children [{:parts [{:text "緊"
                                           :traits #{:char :interactive}}
                                          {:text "- tight\n- strict\n- close at hand\n- near\n- to tighten"
                                           :traits #{:text}}]
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

(defn handle-event [[message cursor]]
  #_(.log js/console (str @cursor))
  (case message
    :click       (om/transact! cursor (fn [c] (update-in c [:traits] #(conj % :hovered))))
    :mouse-enter (om/transact! cursor (fn [c] (update-in c [:traits] #(conj % :hovered))))
    :mouse-leave (om/transact! cursor (fn [c] (update-in c [:traits] #(disj % :hovered))))))

(defn app [app owner]
  (reify
    om/IWillMount
    (will-mount [_])
    om/IRender
    (render [_]
      (html [:svg {:width 1500 :height 1500}
             (grid/cell 5 2 (render-entry app owner))]))))

(defn main []
  (let [event-chan (chan)]
    (go-loop []
      (handle-event (<! event-chan))
      (recur))
    (om/root app app-state {:shared {:event-chan event-chan}
                            :target (. js/document (getElementById "app"))})))
