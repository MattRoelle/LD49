(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.game :as g]
            [app.views :as v]))

(def initial-state
  {:board []
   :w 12
   :h 12
   :animals []
   :inventory [g/chicken]})

(def game-state (r/atom initial-state))

(defn step-game-state! []
  (swap! game-state g/simulate))

(defn root []
  [:div
   (v/game @game-state)
   [:button {:on-click #(step-game-state!)} "Step"]])

(defn mount-app-root []
  (rd/render [root]
             (.getElementById js/document "app-root")))

(mount-app-root)

