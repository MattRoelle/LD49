(ns app.state
  (:require [app.game :as g]
            [reagent.core :as r]))

(def initial-state
  {:board []
   :sz 3
   :day 0
   :animals []
   :moving-animal nil
   :game-over false})

(def game-state (r/atom initial-state))
(def is-simulating (r/atom false))
(def screen (r/atom :in-game))

(defn add-animal-to-inventory! [atype]
  (swap! game-state g/add-animal-to-inventory atype))

(defn pick-up-animal! [id]
  (swap! game-state assoc :moving-animal id))

(defn place-animal-on-board! [pos]
  (swap! game-state g/place-animal pos))

(defn step-game-state! []
  (swap! game-state g/simulate))

(defn next-day! []
  (swap! game-state g/next-day))

(defn set-screen! [s]
  (reset! screen s))

(defn step-sim-until-done!
  ([] 
   (when-not @is-simulating 
     (reset! is-simulating true)
     (step-sim-until-done! 0)))
  ([c]
   (step-game-state!)
   (if (:game-over @game-state)
     (do
       (reset! is-simulating false)
       (js/alert "game over")) ;todo
     (if (<= c (:day-len (:level @game-state)))
       (js/setTimeout #(step-sim-until-done! (inc c)) 150)
       (do
         (next-day!)
         (reset! is-simulating false))))))

(defn load-level! [level]
  (reset! game-state
          (-> initial-state
              (assoc :level level)
              (assoc :sz (:sz level))
              (assoc :animals (mapv g/new-animal (map g/animal-types
                                                      (first (:rounds level))))))))

