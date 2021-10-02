(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.levels :as l]
            [app.game :as g]
            [app.state :as s]
            [app.views :as v]))

(defn root []
  [:div
   (v/game @s/game-state)
   [:button {:on-click #(s/step-game-state!)} "Step"]])

(defn mount-app-root []
  (rd/render [root]
             (.getElementById js/document "app-root")))

(mount-app-root)

(comment
  (s/add-animal-to-inventory! g/chicken)
  (s/load-level! l/level-1)
  (js/console.log "s: " (clj->js @s/game-state)))

