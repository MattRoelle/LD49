(ns app.views)

(def light-green "#92cf72")
(def dark-green "#7ab05d")
(def cell-size 50)

(defn animal [animal]
  (let [position (:pos animal)
        x (first position)
        y (second position)]
    [:div {:key (:id animal)
           :style {:position "absolute"
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

(defn farm [state]
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
                 :border "4px solid black"
                 :border-radius 6
                 :margin 10
                 :width (* cell-size (:w state))
                 :height (* cell-size (:h state))
                 :position "relative"}}
   (map animal (:animals state))])

(defn inventory-item [item]
  [:div {:style {:background-color "rgba(0, 0, 0, 0.1)"
                 :padding 5}}
   [:p {:style {:font-size 20
                :cursor "pointer"}} (:name item)]])

(defn inventory [state]
  [:div {:style {:background-color "#dba469"
                 :padding 20
                 :margin 10
                 :border "4px solid black"
                 :border-radius 6
                 :width 200
                 :height (* cell-size (:h state))}}
   (map inventory-item (:inventory state))])

(defn game [state]
  [:div {:style {:display "flex"
                 :width "auto"
                 :justify-content "center"
                 :margin "0 auto"}}
   (farm state)
   (inventory state)])