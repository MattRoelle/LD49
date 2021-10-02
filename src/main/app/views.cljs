(ns app.views
  (:require [app.game :as g]
            [app.state :as s]
            [reagent.core :as r]))

(def light-green "#92cf72")
(def dark-green "#7ab05d")
;(def cell-size 50)
(def board-size 600)

(defn get-cell-size [state]
  (/ board-size (:sz state)))

(defn animal [state a]
  (let [cell-size (get-cell-size state)]
    [:div {:key (:id a)
           :style {:position "absolute"
                   :z-index 50
                   :width cell-size
                   :height cell-size
                   :transition "all 100ms ease-out"
                   :background-color "white"
                   :border-radius 100
                   :transform (str "translate3d(" (* cell-size (first (:pos a))) "px," (* cell-size (second (:pos a))) "px,0px)")}}]))

(defn get-board-position [state pointer-event]
  (let [client-rect (.getBoundingClientRect (.-target pointer-event))
        offset-x (.-x client-rect)
        offset-y (.-y client-rect)
        click-x (.-pageX pointer-event)
        click-y (.-pageY pointer-event)
        local-x (- click-x offset-x)
        local-y (- click-y offset-y)
        cell-x (js/Math.floor (/ local-x (get-cell-size state)))
        cell-y (js/Math.floor (/ local-y (get-cell-size state)))]
    [cell-x cell-y]))

(defn farm-click-handler [state pointer-event]
  (let [board-position (get-board-position state pointer-event)]
    (s/place-animal-on-board! board-position)))

(defn farm [state]
  (let [cell-size (get-cell-size state)]
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
                   :outline "4px solid black"
                   :border-radius 4
                   :margin 10
                   :width board-size
                   :height board-size
                   :transition "all 300ms ease-in-out"
                   :position "relative"}
           :on-click #(farm-click-handler state %)}
     (map (partial animal state) (g/active-animals state))]))

(defn inventory-animal [state a]
  [:div {:key (:id a)
         :style (merge
                 {:background-color "rgba(0, 0, 0, 0.1)"
                  :padding 5
                  :margin-top 10}
                 (when
                  (= (:moving-animal state) (:id a))
                   {:border "2px solid blue"}))
         :on-click #(s/pick-up-animal! (:id a))}
   [:p {:style {:font-size 20
                :cursor "pointer"}} (:name (:atype a))]])

(defn inventory [state]
  [:div {:style {:background-color "#dba469"
                 :padding 20
                 :margin 10
                 :outline "4px solid black"
                 :border-radius 4
                 :width 200
                 :height board-size}}
   (map (partial inventory-animal state)
        (g/inv-animals state))])

(def mouse-pos (r/atom [0 0]))

(defn cursor-animal [state]
  (let [cell-size (get-cell-size state)
        moving-animal-id (:moving-animal state)
        moving-animal (when moving-animal-id
                        (first
                         (filter #(= (:id %) moving-animal-id)
                                 (:animals state))))]
    (when moving-animal
      [:div {:style {:position "fixed"
                     :z-index 100
                     :width cell-size
                     :height cell-size
                     :background-color "white"
                     :border-radius 100
                     :transform (str "translate3d(" (* cell-size (first @mouse-pos)) "px," (second @mouse-pos) "px,0px)")}}])))

(defonce mousemove-hook
  (.addEventListener
   js/window
   "mousemove"
   (fn [e]
     (reset! mouse-pos [(.-x e) (.-y e)]))))

(defn game [state]
  [:div {:style {:display "flex"
                 :width "auto"
                 :justify-content "center"
                 :margin "0 auto"}}
   (farm state)
   (inventory state)
   (cursor-animal state)])