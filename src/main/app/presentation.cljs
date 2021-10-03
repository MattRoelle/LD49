(ns app.presentation
  (:require [app.game :as g]
            [app.state :as s]
            [reagent.core :as r]
            ["lottie-react" :as Lottie]))

(js/console.log Lottie)

(defn anim [k]
  [(r/adapt-react-class Lottie/default)
   {:animationData (aget (.-animations (.-ld49 js/window)) k)}])

(def light-green "#92cf72")
(def dark-green "#7ab05d")
(def btn-primary "#edae47")
(def btn-side "#9c5a28")
(def btn-hl "#fac446")
(def board-size 600)

(def mouse-pos (r/atom [0 0]))
(defonce mousemove-hook
  (.addEventListener
   js/window
   "mousemove"
   (fn [e]
     (reset! mouse-pos [(.-x e) (.-y e)]))))

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
                   :transform (str "translate3d(" (* cell-size (first (:pos a))) "px," (* cell-size (second (:pos a))) "px,0px)")}}
     (anim "chicken-idle")]))

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

(def board-hover-pos (r/atom nil))
(defn farm-mouse-move-handler [state pointer-event]
  (let [[bx by] (get-board-position state pointer-event)
        max-size (- (:sz state) 1)
        bx (min bx max-size)
        by (min by max-size)
        bx (max bx 0)
        by (max by 0)]
    (reset! board-hover-pos [bx by])))

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
           :on-click #(farm-click-handler state %)
           :on-mouse-move #(farm-mouse-move-handler state %)}
     (map (partial animal state) (g/active-animals state))]))

(defn button [label on-click]
  [:div {:class "btn"
         :on-click on-click
         :style {:background-color btn-primary
                 :hover {:background-color btn-hl}
                 :margin-top 20
                 :padding 20
                 :text-align "center"
                 :border-right (str "4px solid" btn-side)
                 :border-bottom (str "4px solid" btn-side)
                 :border-top (str "2px solid" btn-hl)
                 :border-left (str "2px solid" btn-hl)
                 :box-shadow "4px 4px 0px 0px rgba(0, 0, 0, 0.1)"
                 :border-radius 4}}
   [:p {:style {:font-size 18}} label]])

(defn inventory-animal [state animal-list]
  [:div {:key (:name (:atype (first animal-list)))
         :style (merge
                 {:background-color "rgba(0, 0, 0, 0.1)"
                  :padding 5
                  :margin-top 10
                  :display "flex"
                  :justify-content "center"
                  :align-items "center"}
                ;;  (when
                ;;   (= (:moving-animal state) (:id a))
                ;;    {:border "2px solid blue"})
                 )
         :on-click #(s/pick-up-animal! (first animal-list))}
   [:div {:style {:width 50}} (anim "chicken-idle")]
   [:p {:style {:margin-left 10 :font-size 32}} (str "x" (count animal-list))]])

(defn animal-groups [state]
  (map (fn [[k group]]
         (inventory-animal state group))
       (group-by #(:atype %) (g/inv-animals state))))

(defn start-day-btn [state]
  (if (> (count (g/inv-animals state)) 0)
    [:p {:style {:text-align "center" :font-size 22 :margin-top 20}} "Finish placing animals"]
    (button "Start Day" #(s/step-sim-until-done!))))

(defn game-time [state]
  [:p {:style {:text-align "center" :font-size 22 :margin-top 20}} (str @s/game-time ":00" (if (< @s/game-time 12) "AM" "PM"))])

(defn arrow []
  [:div {:dangerouslySetInnerHTML {:__html
                                   "<svg id=\"Layer_1\" data-name=\"Layer 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 420.79 331.3\"><defs><style>.cls-1{fill:#fff;stroke:#58595b;stroke-linecap:round;stroke-linejoin:round;stroke-width:11px;}</style></defs><polygon class=\"cls-1\" points=\"255.14 325.8 255.14 214.89 5.5 250.24 5.5 81.06 255.14 116.41 255.14 5.5 415.29 165.65 255.14 325.8\"/></svg>"}}])

(defn inventory [state]
  [:div {:style {:background-color "#dba469"
                 :padding 20
                 :margin 10
                 :outline "4px solid black"
                 :border-radius 4
                 :width 200
                 :height board-size}}

   (animal-groups state)

   (if (= @s/is-simulating true)
     (game-time state)
     (start-day-btn state))])

;; (defn rotate-vector [v n]
;;   (loop [out (cons (rest v) (first v))
;;          c 0]
;;     (if (>= c n)
;;       out
;;       (recur out (inc c)))))

(defn animal-path-preview [state animal]
  (let [path (:path (:atype animal))]
        ;path (rotate-vector starting-path 2)]
    (js/console.log "p"(clj->js path))
    [:div]))

(defn cursor-animal [state]
  (let [cell-size (get-cell-size state)
        moving-animal-id (:moving-animal state)
        moving-animal (when moving-animal-id
                        (first
                         (filter #(= (:id %) moving-animal-id)
                                 (:animals state))))]
    (when moving-animal
      [:div
       (animal-path-preview state moving-animal)
       [:div {:id "moving-animal"
                   :style {:pointer-events "none"
                           :position "fixed"
                           :top (* -0.5 cell-size)
                           :left (* -0.5 cell-size)
                           :z-index 100
                           :width cell-size
                           :height cell-size
                           :transform (str "translate3d(" (first @mouse-pos) "px," (second @mouse-pos) "px,0px) scale(0.5, 0.5)")}}
             (anim "chicken-idle")]])))

(defn loading []
  [:h1 "Loading..."])

(defn game [state]
  [:div {:style {:display "flex"
                 :width "auto"
                 :justify-content "center"
                 :margin "0 auto"}}
   (farm state)
   (inventory state)
   (cursor-animal state)])

(defn root []
  (cond
    (= @s/screen :loading) (loading)
    :else (game @s/game-state)))