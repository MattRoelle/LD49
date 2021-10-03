(ns app.presentation
  (:require [app.game :as g]
            [reagent.core :as r]
            ["gsap" :as gsap]
            ["lottie-react" :as Lottie]))

(js/console.log gsap)

(def initial-state
  {:board []
   :sz 3
   :day 0
   :animals []
   :moving-animal nil
   :game-over false})

(def light-green "#92cf72")
(def dark-green "#7ab05d")
(def btn-primary "#edae47")
(def btn-side "#9c5a28")
(def btn-hl "#fac446")
(def board-size 600)

(def start-time 5)
(def n-hours 12)

(def game-state (r/atom initial-state))
(def game-time (r/atom start-time))
(def is-simulating (r/atom false))
(def screen (r/atom :in-game))
(def mouse-pos (r/atom [0 0]))
(def board-hover-pos (r/atom nil))
(def game-over (r/atom false))

(defn add-animal-to-inventory! [atype]
  (swap! game-state g/add-animal-to-inventory atype))

(defn pick-up-animal! [animal]
  (swap! game-state assoc :moving-animal (:id animal)))

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
     (reset! game-time start-time)
     (step-sim-until-done! 0)))
  ([c]
   (step-game-state!)
   (swap! game-time inc)
   (if (:game-over @game-state)
     (reset! is-simulating false)
     (if (<= c (+ start-time n-hours))
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

(defn anim [k]
  [(r/adapt-react-class Lottie/default)
   {:animationData (aget (.-animations (.-ld49 js/window)) k)}])

(defn arrow []
  [:div {:style {:width "100%" :height "100%"}
         :dangerouslySetInnerHTML {:__html
                                   "<svg id=\"Layer_1\" data-name=\"Layer 1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 420.79 331.3\"><defs><style>.cls-1{fill:#fff;stroke:#58595b;stroke-linecap:round;stroke-linejoin:round;stroke-width:11px;}</style></defs><polygon class=\"cls-1\" points=\"255.14 325.8 255.14 214.89 5.5 250.24 5.5 81.06 255.14 116.41 255.14 5.5 415.29 165.65 255.14 325.8\"/></svg>"}}])

(defonce mousemove-hook
  (.addEventListener
   js/window
   "mousemove"
   (fn [e]
     (reset! mouse-pos [(.-x e) (.-y e)]))))

(defn get-cell-size [state]
  (/ board-size (:sz state)))

(defn farm-animal [state a]
  (let [cell-size (get-cell-size state)
        id (:id a)]
    [:div {:id id
           :key id
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
    (place-animal-on-board! board-position)))

(defn farm-mouse-move-handler [state pointer-event]
  (let [[bx by] (get-board-position state pointer-event)
        max-size (- (:sz state) 1)
        bx (min bx max-size)
        by (min by max-size)
        bx (max bx 0)
        by (max by 0)]
    (reset! board-hover-pos [bx by])))

(defn directional-arrow [state [x y] [dx dy]]
  (let [cell-size (get-cell-size state)
        transform (cond (< dx 0) "rotate(180deg) translate3d(0, -20px, 0)"
                        (> dx 0) "rotate(0deg) translate3d(0, 20px, 0)"
                        (< dy 0) "rotate(-90deg)  translate3d(-20px, 0, 0)"
                        (> dy 0) "rotate(90deg)  translate3d(20px, 0, 0)")]
    [:div {:style {:position "absolute"
                   :left (* x cell-size)
                   :top (* y cell-size)
                  ; :opacity 0.5
                   :width (/ cell-size 2)
                   :height (/ cell-size 2)
                   :transform transform}}
     (arrow)]))

(defn animal-path-preview [state]
  (when-let [moving-animal (g/get-moving-animal state)]
    ;; (js/console.log (clj->js @board-hover-pos))
    (let [path (:path (:atype moving-animal))
          walked-path (g/walk-path path @board-hover-pos)]
      ;; (js/console.log (clj->js path))
      (map (fn [input]
             (directional-arrow state (first input) (second input)))
           walked-path))))

(defn game-over-highlight [state]
  (js/console.log "(:collisions state)" (clj->js (:collisions state)))
  (let [cell-size (get-cell-size state)]
    (map (fn [{[x y] :pos}]
          [:div {:style {:position "absolute"
                         :left (* x cell-size)
                         :top (* y cell-size)
                         :width cell-size
                         :height cell-size
                         :background-color "rgba(230, 40, 40, 0.3)"}}])
        (:collisions state))))

(defn farm [state]
  (let [cell-size (get-cell-size state)]
    [:div {:id "farm-root"
           :style {:background-color dark-green
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
     (map (partial farm-animal state) (g/active-animals state))
     (animal-path-preview state)
     (when (:game-over state) (game-over-highlight state))]))

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
         :on-click #(pick-up-animal! (first animal-list))}
   [:div {:style {:width 50}} (anim "chicken-idle")]
   [:p {:style {:margin-left 10 :font-size 32}} (str "x" (count animal-list))]])

(defn animal-groups [state]
  (map (fn [[k group]]
         (inventory-animal state group))
       (group-by #(:atype %) (g/inv-animals state))))

(defn start-day-btn [state]
  (if (> (count (g/inv-animals state)) 0)
    [:p {:style {:text-align "center" :font-size 22 :margin-top 20}} "Finish placing animals"]
    (button "Start Day" #(step-sim-until-done!))))

(defn game-clock [state]
  [:p {:style {:text-align "center"
               :font-size 22
               :margin-top 20}}
   (if (< @game-time 12)
     (str @game-time ":00" "AM")
     (str (if (> @game-time 12)  (- @game-time 12) "12")
          ":00"
          "PM"))])

(defn inventory [state]
  [:div {:style {:background-color "#dba469"
                 :padding 20
                 :margin 10
                 :outline "4px solid black"
                 :border-radius 4
                 :width 200
                 :height board-size}}

   (animal-groups state)

   (if (= @is-simulating true)
     (game-clock state)
     (start-day-btn state))])

;; (defn rotate-vector [v n]
;;   (loop [out (cons (rest v) (first v))
;;          c 0]
;;     (if (>= c n)
;;       out
;;       (recur out (inc c)))))



(defn cursor-animal [state]
  (let [cell-size (get-cell-size state)
        moving-animal (g/get-moving-animal state)]
    (when moving-animal
      [:div
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

(defn game-over! [state]
  (reset! game-over true)
  (let [a (:crazy-animal state)
        animals (:animals state)
        other-animals (filter #(not= (:id %) (:id a)) animals)
        animals-dom (apply merge
                           (map (fn [x]
                                  {(:id x) (.getElementById js/document (:id x))})
                                animals))
        a-dom (get animals-dom (:id a))
        farm-root-dom (.getElementById js/document "farm-root")
        fr-cr (.getBoundingClientRect farm-root-dom)
        ox (.-x fr-cr)
        oy (.-y fr-cr)]
    (letfn [(done [])
            (on-complete [ix]
              (if (< ix (- (count other-animals) 1))
                (tween-to-animal (inc ix))
                (done)))
            (tween-to-animal [ix]
              (let [target (nth other-animals ix)
                    target-dom (get animals-dom (:id target))
                    cr (.getBoundingClientRect target-dom)
                    tx (- (.-x cr) ox)
                    ty (- (.-y cr) oy)
                    duration 0.8]
                (.to gsap/gsap a-dom
                     #js {:x tx
                          :y ty
                          :ease "back.inOut(10)"
                          :duration duration
                          :onComplete #(on-complete ix)})
                (js/setTimeout
                 #(.to gsap/gsap target-dom #js{:rotation 360
                                                :scale 0
                                                :duration 0.75
                                                :y 0})
                 (* duration 600))))]
      (.to gsap/gsap a-dom
           #js {:rotation 720
                :duration 1
                :delay 0.5
                :onComplete #(tween-to-animal 0)})))
  [:div])

(defn game [state]
  [:div {:style {:display "flex"
                 :width "auto"
                 :justify-content "center"
                 :margin "0 auto"}}
   (farm state)
   (inventory state)
   (cursor-animal state)
   (when (and (:game-over state) (not @game-over)) (game-over! state))])

(defn root []
  (cond
    (= @screen :loading) (loading)
    :else (game @game-state)))