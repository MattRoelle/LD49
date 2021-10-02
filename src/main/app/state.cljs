(ns app.state
  (:require [app.game :as g]
            [reagent.core :as r]))

(def initial-state
  {:board []
   :sz 3
   :day 0
   :day-len 11
   :animals []
   :moving-animal nil})

(def game-state (r/atom initial-state))

(defn add-animal-to-inventory! [atype]
  (swap! game-state g/add-animal-to-inventory atype))

(defn pick-up-animal! [id]
  (swap! game-state assoc :moving-animal id))

(defn place-animal-on-board! [pos]
  (swap! game-state g/place-animal pos))

(defn step-game-state! []
  (swap! game-state g/simulate))

(defn load-level! [level]
  (reset! game-state
          (-> initial-state
              (assoc :sz (:sz level))
              (assoc :day-len (:day-len level))
              (assoc :animals (mapv g/new-animal (map g/animal-types
                                                     (first (:rounds level))))))))