(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.levels :as l]
            [app.game :as g]
            [app.state :as s]
            [app.presentation :as p]))

(defn root []
  [:div
   (p/game @s/game-state)
   [:button {:on-click #(s/step-game-state!)} "Step"]])

(defn mount-app-root []
  (rd/render [root]
             (.getElementById js/document "app-root")))

(mount-app-root)

(comment
  (s/add-animal-to-inventory! g/chicken)
  (s/load-level! l/level-1)
  (s/load-level! l/level-2)
  (js/console.log (clj->js @s/game-state)))


(defonce init
  (do
    (s/load-level! l/level-1)))
