(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.game :as g]
            [app.views :as v]))

(def initial-state
  {:board []
   :w 8
   :h 8
   :animals [(g/FarmAnimal. (random-uuid) g/chicken [4 4] 0)
             (g/FarmAnimal. (random-uuid) g/chicken [4 4] 2)]})

(def game-state (r/atom initial-state))

(defn step-game-state! []
  (swap! game-state g/simulate))

(defn root []
  [:div
   (v/farm @game-state)
   [:button {:on-click #(step-game-state!)} "Step"]])

(defn mount-app-root []
  (rd/render [root]
             (.getElementById js/document "app-root")))

(mount-app-root)

