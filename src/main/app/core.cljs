(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.levels :as l]
            [app.game :as g]
            [app.presentation :as p]))


(defn mount-app-root []
  (rd/render [p/root]
            (.getElementById js/document "app-root")))

(mount-app-root)

(comment
  (p/add-animal-to-inventory! g/chicken)
  (g/rotate-vector [[-1 0] [1 0] [0 1]] 0)
  (p/load-level! l/level-1)
  (p/load-level! l/level-4)
  (js/console.log (clj->js @p/game-state))
  (reset! p/is-simulating ))