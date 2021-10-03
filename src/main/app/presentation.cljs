
(ns app.presentation
  (:require [app.game :as g]
            [app.levels :as l]
            [reagent.core :as r]
            ["gsap" :as gsap]
            ["lottie-react" :as Lottie]))

(js/console.log gsap)

(def initial-state
  {:board []
   :sz 3
   :day 0
   :level-number 0
   :animals []
   :moving-animal nil
   :game-over false})

(def light-green "#92cf72")
(def dark-green "#7ab05d")
(def btn-primary "#edae47")
(def btn-side "#9c5a28")
(def btn-hl "#fac446")
(def game-size 800)
(def board-size 600)

(def start-time 5)
(def n-hours 8)

(defonce screen (r/atom :title))
(defonce game-state (r/atom initial-state))
(defonce game-time (r/atom start-time))
(defonce is-simulating (r/atom false))
(defonce mouse-pos (r/atom [0 0]))
(defonce board-hover-pos (r/atom nil))
(defonce game-over (r/atom false))
(defonce doing-game-over-animation (r/atom false))
(defonce show-tutorial (r/atom true))

(defonce started-playing-music? (atom false))
(defonce theme (new js/Audio "./theme.mp3"))
(defonce global-click (.addEventListener js/window "click"
                                         #(when (not @started-playing-music?)
                                            (aset theme "loop" true)
                                            (.play theme)
                                            (reset! started-playing-music? true))))
(defn main-menu! []
  (reset! game-state initial-state)
  (reset! screen :title))

(defn add-animal-to-inventory! [atype]
  (swap! game-state g/add-animal-to-inventory atype))

(defn pick-up-animal! [animal]
  (swap! game-state assoc :moving-animal (:id animal)))

(defn place-animal-on-board! [pos]
  (swap! game-state g/place-animal pos))

(defn move-animal-back-to-inv! [state a]
  (let [id (:id a)
        ix-map (apply merge (map-indexed (fn [ix a2] {(:id a2) ix}) (:animals state)))]
    (js/console.log "ix-map" (clj->js ix-map))
    (swap! game-state assoc-in [:animals (get ix-map id)] (assoc a :pos :inv))))

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
       (js/setTimeout #(step-sim-until-done! (inc c)) 200)
       (do
         (next-day!)
         (reset! is-simulating false))))))

(defn load-level! [level]
  (reset! screen :in-game)
  (reset! game-over false)
  (reset! doing-game-over-animation false)
  (reset! game-state
          (-> initial-state
              (assoc :level-number (:level-number @game-state))
              (assoc :level level)
              (assoc :sz (:sz level))
              (assoc :animals (mapv g/new-animal
                                    (map g/animal-types
                                         (first (:rounds level))))))))

(defn next-level! []
  (let [n (inc (:level-number @game-state))]
    (if (< n (count l/levels))
      (do
        (swap! game-state assoc :level-number n)
        (load-level! (get l/levels n))
        (reset! screen :in-game))
      (reset! screen :victory))))

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



(defn get-board-position [state pointer-event]
  (let [client-rect (.getBoundingClientRect (.getElementById js/document "farm-root"))
        offset-x (.-x client-rect)
        offset-y (.-y client-rect)
        mouse-x (.-pageX pointer-event)
        mouse-y (.-pageY pointer-event)
        local-x (- mouse-x offset-x)
        local-y (- mouse-y offset-y)
        cell-x (js/Math.floor (/ local-x (get-cell-size state)))
        cell-y (js/Math.floor (/ local-y (get-cell-size state)))]
    [cell-x cell-y]))

(defn farm-click-handler [state pointer-event]
  (let [board-position (get-board-position state pointer-event)]
    (place-animal-on-board! board-position)))

(defn farm-mouse-move-handler [state pointer-event]
  (let [[hx hy] (get-board-position state pointer-event)
        max-size (- (:sz state) 1)
        bx (min hx max-size)
        by (min hy max-size)
        bx (max bx 0)
        by (max by 0)]
    (reset! board-hover-pos [bx by])))

(defn directional-arrow [state [x y] [dx dy]]
  (let [cell-size (get-cell-size state)
        transform (cond (< dx 0) "rotate(180deg)"
                        (> dx 0) "rotate(0deg)"
                        (< dy 0) "rotate(-90deg)"
                        (> dy 0) "rotate(90deg)")
        farm-dom (.getElementById js/document "farm-root")
        cr (.getBoundingClientRect farm-dom)
        cx (.-x cr)
        cy (.-y cr)
        ;; (cond (< dx 0) "rotate(180deg) translate3d(0, -20px, 0)"
        ;;       (> dx 0) "rotate(0deg) translate3d(0, 20px, 0)"
        ;;       (< dy 0) "rotate(-90deg)  translate3d(-20px, 0, 0)"
        ;;       (> dy 0) "rotate(90deg)  translate3d(20px, 0, 0)")
        ]
    [:div {:key (str x y dx dy)
           :style {:position "fixed"
                   :left (+ cx  (* x cell-size))
                   :top (+ cy (* y cell-size))
                  ; :opacity 0.5
                   :width (/ cell-size 2)
                   :height (/ cell-size 2)
                   :transform transform}}
     (arrow)]))

(defn animal-path-preview [state a pos]
  (let [path (:path (:atype a))
        walked-path (g/walk-path path pos (:ix a))]
      ;; (js/console.log (clj->js path))
    (map (fn [input]
           (directional-arrow state (first input) (second input)))
         walked-path)))

(defn moving-animal-path-preview [state]
  (when-let [moving-animal (g/get-moving-animal state)]
    (animal-path-preview state moving-animal @board-hover-pos)))

(defn farm-animal [state game-over doing-game-over-animation is-simulating a]
  (let [cell-size (get-cell-size state)
        [x y] (:pos a)
        x (* cell-size x)
        y (* cell-size y)
        id (:id a)]
    [:div {:key id
           :class (str "farm-animal "
                       (when (and (not game-over)
                                  (not is-simulating))
                         "idle"))}
     [:div {:id id

            :style (merge {:position "absolute"
                           :z-index 50
                           :width cell-size
                           :height cell-size
                           :transform (str "translate3d(" x "px," y "px,0px) "
                                           "scaleX(" (if (:flipped a) "-1" "1") ")")}
                          (when (not doing-game-over-animation) {:transition "all 150ms ease-in-out"}))}
      (anim (:anim-key (:atype a)))
      (when (:pending a)
        [:p {:on-click #(move-animal-back-to-inv! state a)
             :style {:z-index 300 :background "rgba(0, 0, 0, 0.5)" :padding 4 :margin 0 :position "absolute" :cursor :pointer :top 0 :right 0 :font-size 30 :line-height "30px" :color "white"}} "X"])]
     [:div {:class "path-preview" :position "fixed" :top 0 :left 0}
      (animal-path-preview state a (:pos a))]]))

(defn game-over-highlight [state]
  (js/console.log "(:collisions state)" (clj->js (:collisions state)))
  (let [cell-size (get-cell-size state)
        [_ [x y]] (first (:collisions state))]
    [:div {:class "game-over-highlight"
           :style {:position "absolute"
                   :left (* x cell-size)
                   :top (* y cell-size)
                   :width cell-size
                   :height cell-size
                   :background-color "rgba(230, 40, 40, 0.3)"}}]))

(defn hover-highlight [state]
  (let [cell-size (get-cell-size state)
        [x y] @board-hover-pos]
    [:div {:style {:position "absolute"
                   :width cell-size
                   :height cell-size
                   :border "4px solid white"
                   :top (* y cell-size)
                   :left (* x cell-size)}}]))

(defn farm [state]
  (let [cell-size (get-cell-size state)]
    [:div {:id "farm-root"
           :class (when (:game-over state) "farm-game-over")
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
                   :margin-right 20
                   :width board-size
                   :height board-size
                   :transition "all 300ms ease-in-out"
                   :position "relative"}
           :on-click #(farm-click-handler state %)
           :on-mouse-move #(farm-mouse-move-handler state %)}
     (map (partial farm-animal state @game-over @doing-game-over-animation @is-simulating) (g/active-animals state))
     (moving-animal-path-preview state)
     (hover-highlight state)
     (when (:game-over state) (game-over-highlight state))]))

(defn button
  ([label on-click] (button label on-click {}))
  ([label on-click styles]
   [:div {:class "game-btn"
          :key label
          :on-click on-click
          :style (merge {:background-color btn-primary
                         :hover {:background-color btn-hl}
                         :margin-top 20
                         :padding 20
                         :cursor "pointer"
                         :text-align "center"
                         :border-right (str "4px solid" btn-side)
                         :border-bottom (str "4px solid" btn-side)
                         :border-top (str "2px solid" btn-hl)
                         :border-left (str "2px solid" btn-hl)
                         :box-shadow "4px 4px 0px 0px rgba(0, 0, 0, 0.1)"
                         :border-radius 4} styles)}
    [:p {:style {:font-size 18}} label]]))

(defn inventory-animal [state animal-list]
  (let [a (first animal-list)
        atype (:atype a)
        moving-animal (g/get-moving-animal state)]
    [:div {:key (:name atype)
           :class "inventory-animal"
           :style (merge
                   {:background-color "rgba(0, 0, 0, 0.1)"
                    :padding 5
                    :margin-top 10
                    :border-radius 4
                    :cursor "pointer"
                    :display "flex"
                    :justify-content "center"
                    :align-items "center"}
                   (when
                    (= (:atype moving-animal) atype)
                     {:outline "4px solid yellow"}))
           :on-click #(pick-up-animal! (first animal-list))}
     [:div {:style {:width 50}} (anim (:anim-key atype))]
     [:p {:style {:margin-left 10 :font-size 32}} (str "x" (count animal-list))]]))

(defn animal-groups [state is-simulating]
  [:div

   (when (and (not (:game-over state)) (not is-simulating)) (button "Restart" #(load-level! (:level state))))
   (when (:moving-animal state)
     (button "Cancel" #(swap! game-state assoc :moving-animal nil)))
   (map (fn [[k group]]
          (inventory-animal state group))
        (group-by #(:atype %) (g/inv-animals state)))])

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

(defn header [state]
  [:div {:style {:background-color "#dba469"
                 :display "flex"

                 :padding 20
                 :margin-bottom 20
                 :outline "4px solid black"
                 :flex-direction "row"
                 :align-items "center"
                 :justify-content "center"
                 :border-radius 4
                 :width "100%"}}
   [:h2 {:style {:flex 1}} (:name (:level state))]
   (button "Help" #(reset! show-tutorial true) {:margin 0})
   (button "Menu" #(main-menu!) {:margin-left 10 :margin-top 0})])

(defn sidebar [state]
  [:div {:style {:background-color "#dba469"
                 :padding 20
                 :outline "4px solid black"
                 :border-radius 4
                 :flex 1
                 :height board-size}}
   [:h2 (str "Day " (inc (:day state)) " of " (count (:rounds (:level state))))]
   (when  @is-simulating (game-clock state))
   (animal-groups state @is-simulating)

   (when (= @is-simulating false)
     (if (:game-over state)
       [:div
        [:h3 {:style {:margin 20}} "Game Over"]
        (button "Try again" #(load-level! (:level state)))]
       (if (:win state)
         [:div
          [:h1 {:style {:margin-top 20}} "Level Complete"]
          (button "Next Level!" #(next-level!))]
         (start-day-btn state))))])




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
        (anim (:anim-key (:atype moving-animal)))]])))

(defn loading []
  [:h1 "Loading..."])

(defn game-over! [state]
  (reset! game-over true)
  (let [a (:crazy-animal state)
        animals (:animals state)
        cell-size (get-cell-size state)
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
                    duration 0.5]
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
                                                :y (* 1000 (.random js/Math))
                                                :x (* 1000 (.random js/Math))})
                 (* duration 600))))]
      (js/setTimeout (fn []
                       (reset! doing-game-over-animation true)
                       (.to gsap/gsap a-dom
                            #js {:rotation 1100
                                 :duration 1
                                 :scale 1.2
                                 :x 0
                                 :y 0
                                 :delay 0.5
                                 :ease "bounce.inOut"
                                 :onComplete #(tween-to-animal 0)})) 500)))
  [:div])

(defn tutorial-screen []
  [:div {:style {:width 600
                 :margin "0 auto"
                 :position "absolute"
                 :top (if @show-tutorial 100 -1000)
                 :border-radius 4
                 :z-index 2000
                 :transition "all 500ms ease-in-out"
                 :border "4px solid black"
                 :background "#dba469"
                 :padding 40}}
   [:h1 {:style {:text-align "center" :margin-bottom 30}} "Welcome to The Funny Farm"]
   [:p {:style {:font-size 20 :margin-bottom 20 :line-height "28px"}} "The funny farm is a home for mentally unstable farm animals. It's where farmers send their stressed out animals to relax."]
   [:p {:style {:font-size 20 :margin-bottom 20 :line-height "28px"}} "These animals " [:strong "LOVE "] "their routines. Each day new animals arrive at the farm. Arrange them on the board so that they may all go about their days uninterrupted."]
   [:p {:style {:font-size 20 :margin-bottom 20 :line-height "28px"}} "You " [:strong "DO NOT "] "want to find out what happens when one of their routines gets interrupted..."]
   [:p {:style {:font-size 20 :margin-bottom 20 :line-height "28px"}} "Good luck!"]
   (button "Got it" #(reset! show-tutorial false))])

(defn game-screen [state]
  [:div {:style {:width "100%"
                 :height "100%"
                 :padding-top 40
                 :background "#56c0e3"}}
   [:div {:style {:width game-size
                  :margin "0 auto"}}
    (header state)
    [:div {:style {:display "flex"
                   :width "100%"
                   :height "100%"
                   :justify-content "center"
                   :margin "0 auto"}}
     (tutorial-screen)
     (farm state)
     (sidebar state)
     (cursor-animal state)
     (when (and (:game-over state) (not @game-over)) (game-over! state))]]])

(def text-outline "-2px -2px 0 #000, 2px -2px 0 #000, -2px 2px 0 #000, 2px 2px 0 #000")
(def text-outline2 "-1px -1px 0 white, 1px -1px 0 white, -1px 1px 0 white, 1px 1px 0 #000")

(defn title-screen []
  [:div {:style {:display "flex"
                 :width "100%"
                 :height "100%"
                 :justify-content "center"
                 :margin "0 auto"
                 :background "#56c0e3"}}
   [:div {:style {:width 600 :height 600 :margin 30 :border "4px solid black" :text-align "center" :padding 40

                  :background-color dark-green
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
                  :background-position (str "0 0, 50px 50px")
                  :background-size  "100px 100px"}}
    [:h1 {:style {:margin-bottom 50 :font-size 40 :color "white" :font-weight "bold" :text-shadow text-outline}} "The Funny Farm"]
    
    [:div {:style {:display "flex" :width "80%" :margin "0 auto"}}
     (anim "chicken-idle")
     (anim "chicken2-idle")
     (anim "cow-idle")
     (anim "pig-idle")]
    (button "Play" #(load-level! (first l/levels)))
    (button "Levels" #(reset! screen :level-select))
    [:div {:style {:background "white" :padding 5 :border-radius 10 :margin-top 30}}
     [:p {:style {:color "black" :font-size 24}} "Made in 48hours for Ludum Dare 49"]
     [:p {:style {:color "black" :font-size 20 :margin-top 10}} "By Matt Roelle"]]]])

(defn level-select-screen []
  [:div {:style {:display "flex"
                 :width "100%"
                 :height "100%"
                 :justify-content "center"
                 :margin "0 auto"
                 :background "#56c0e3"}}
   [:div {:style {:width 600 :text-align "center" :padding 40}}
    [:div {:style {:margin-bottom 50 :display "flex" :justify-content "flex-end" :align-items "flex-end"}}
     [:h1 {:style {:flex 1}} "Select Level"]
     (button "Back" #(reset! screen :title))]
    (map-indexed (fn [lix level]
                   (button (:name level)
                           (fn []
                             (swap! game-state assoc :level-number lix)
                             (load-level! level))
                           {:display "inline-block" :margin 10 :width "45%"}))
                 l/levels)]])


(defn victory-screen []
  [:div {:style {:display "flex"
                 :width "100%"
                 :height "100%"
                 :justify-content "center"
                 :margin "0 auto"
                 :background "#56c0e3"}}
   [:div {:style {:width 400 :text-align "center" :padding 40}}
    [:p {:style {:margin-bottom 50}} "Thanks for playing!"]
    (button "Main Menu" #(main-menu!))]])

(defn root []
  (cond
    (= @screen :title) (title-screen)
    (= @screen :victory) (victory-screen)
    (= @screen :level-select) (level-select-screen)
    :else (game-screen @game-state)))