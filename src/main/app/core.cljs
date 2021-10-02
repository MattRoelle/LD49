(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.game :as g]
            [app.views :as v]))

(def initial-state
  {:board []
   :w 8
   :h 8
   :animals [(g/FarmAnimal. (random-uuid) g/chicken2 [5 5] 0)
             (g/FarmAnimal. (random-uuid) g/chicken2 [1 5] 3)
             (g/FarmAnimal. (random-uuid) g/chicken2 [5 4] 0)
             (g/FarmAnimal. (random-uuid) g/chicken2 [1 4] 3)
             (g/FarmAnimal. (random-uuid) g/chicken2 [5 6] 0)
             (g/FarmAnimal. (random-uuid) g/chicken2 [1 6] 3)
             (g/FarmAnimal. (random-uuid) g/chicken2 [5 7] 0)
             (g/FarmAnimal. (random-uuid) g/chicken2 [1 7] 3)
             (g/FarmAnimal. (random-uuid) g/chicken [2 1] 0)
             (g/FarmAnimal. (random-uuid) g/chicken [1 1] 2)
             (g/FarmAnimal. (random-uuid) g/chicken [4 1] 0)
             (g/FarmAnimal. (random-uuid) g/chicken [3 1] 2)
             (g/FarmAnimal. (random-uuid) g/chicken [6 1] 0)
             (g/FarmAnimal. (random-uuid) g/chicken [5 1] 2)]})

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

