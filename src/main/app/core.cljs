(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.levels :as l]
            [app.game :as g]
            [app.presentation :as p]))

(defn root []
  [:div
   (p/root)])

(defn mount-app-root []
  (rd/render [root]
            (.getElementById js/document "app-root")))

(mount-app-root)

(comment
  (g/walk-path [[-1 0] [-1 0] [0 1]] [4 4])
  (p/add-animal-to-inventory! g/chicken)
  (p/load-level! l/level-1)
  (p/load-level! l/level-2)
  (js/console.log (clj->js @p/game-state))
  (reset! p/is-simulating ))


(defonce init
  (do
    (p/load-level! l/level-1)))
