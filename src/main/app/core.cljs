(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.game :as g]))

(def light-green "#92cf72")
(def dark-green "#7ab05d")
(def cell-size 50)

(def initial-state
  {:board []
   :w 8
   :h 8
   :animals [(g/FarmAnimal. (random-uuid) g/chicken [4 4] 0)]})

(defn c-animal [animal]
  (let [position (:pos animal)
        x (first position)
        y (second position)]
    [:div {:key (:id animal)
           :style {:position :relative
                   :z-index 50
                   :width cell-size
                   :height cell-size
                   :transition "all 100ms ease-out"
                   :background-color "white"
                   :border-radius 100
                   :transform (str "translate3d("
                                   (* cell-size x) "px,"
                                   (* cell-size y)
                                   "px,0px)")}}]))

(defn c-farm [state]
  [:div {:style {:background-color dark-green
                 :background-image  (str
                                     "repeating-linear-gradient(
                                     45deg,
                                     " light-green " 25%,
                                     transparent 25%,
                                     transparent 75%,
                                     " light-green " 75%,
                                     " light-green "),
                                     repeating-linear-gradient(45deg, 
                                     " light-green " 25%,
                                     " dark-green " 25%,
                                     " dark-green " 75%,
                                     " light-green " 75%,
                                     " light-green ")")
                 :background-position (str "0 0, " cell-size "px " cell-size "px")
                 :background-size (str (* 2 cell-size) "px " (* 2 cell-size) "px")
                 :border "6px solid black"
                 :border-radius 4
                 :width (* cell-size (:w state))
                 :height (* cell-size (:h state))
                 :margin "50px auto"}}
   (map c-animal (:animals state))])

(def game-state (r/atom initial-state))

(defn step-game-state! []
  (swap! game-state g/simulate))

(defn root []
  [:div
   (c-farm @game-state)
   [:button {:on-click #(step-game-state!)} "Step"]])

(defn mount-app-root []
  (rd/render [root]
             (.getElementById js/document "app-root")))

(mount-app-root)

