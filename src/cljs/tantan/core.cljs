(ns tantan.core
  (:require [tantan.grid :as grid]
            [tantan.traits :refer [render-entry]]
            [cljs.core.async :as async :refer [>! <! chan put! close!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as s]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defonce app-state (atom {:tree {}}))

(defn handle-event [[message cursor]]
  (case message
    :click       (om/transact! cursor (fn [c] (update-in c [:traits] #(conj % :hovered))))
    :mouse-enter (om/transact! cursor (fn [c] (update-in c [:traits] #(conj % :hovered))))
    :mouse-leave (om/transact! cursor (fn [c] (update-in c [:traits] #(disj % :hovered))))))

(comment
  (defn contacts-view [app owner]
    (reify
      om/IInitState
      (init-state [_]
        {:delete (chan)
         :text "打開"})
      om/IWillMount
      (will-mount [_]
        (let [delete (om/get-state owner :delete)]
          (go (loop []
                (let [contact (<! delete)]
                  (om/transact! app :contacts
                                (fn [xs] (vec (remove #(= contact %) xs))))
                  (recur))))))
      om/IRenderState
      (render-state [this state]
        (dom/div nil
                 (dom/h2 nil "Contact list")
                 (apply dom/ul nil
                        (om/build-all contact-view (:contacts app)
                                      {:init-state state}))
                 (dom/div nil
                          (dom/input #js {:type "text" :ref "new-contact" :value (:text state)})
                          (dom/button #js {:onClick #(add-contact app owner)} "Add contact")))))))

(defn fetch-entry [input]
  (go (let [response (<! (http/get (str "/entry/" input)))]
        (swap! app-state #(assoc % :tree (:body response))))))

(defn handle-change [e owner {:keys [text]}]
  (om/set-state! owner :text (.. e -target -value)))

(defn sidebar-component [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:text ""})
    om/IRenderState
    (render-state [this state]
      (html [:div {:style {:width "15%"
                           :height (.-innerHeight js/window)
                           :background-color "#FFD378"
                           :float "left"}}
             [:input {:type "text" :ref "search-box" :value (:text state)
                      :on-change #(handle-change % owner state)}]
             [:button {:on-click #(fetch-entry (:text state))} "GO"]]))))

(defn tree-component [cursor owner]
  (reify
    ;; om/IWillMount
    ;; (will-mount [_])
    om/IRender
    (render [_]
      (html [:svg {:width "84%" :height "1500" :style {:float "left"}}
             (grid/cell (+ (/ (tantan.traits/find-tree-width cursor) 2) 1)
                        1
                        (render-entry cursor owner))]))))

(defn app-component [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div
             (om/build sidebar-component app)
             (om/build tree-component (:tree app))]))))

(defn main []
  (let [event-chan (chan)]
    (go-loop []
      (handle-event (<! event-chan))
      (recur))
    (om/root app-component app-state {:shared {:event-chan event-chan}
                                      :target (. js/document (getElementById "app"))})))
