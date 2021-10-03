(ns app.game)

(defrecord FarmAnimal [id atype pos ix flipped pending])

(def chicken {:name "Chickn" :anim-key "chicken-idle" :path [[-1 0] [0 1] [1 0] [0 -1]]})
(def chicken2 {:name "Brown Chickn" :anim-key "chicken2-idle" :path [[1 0] [1 0] [-1 0] [-1 0]]})
(def cow {:name "Cow" :anim-key "cow-idle" :path [[0 -1] [0 -1] [1 0] [1 0] [0 1] [0 1] [-1 0] [-1 0]]})
(def pig {:name "Pig" :anim-key "pig-idle" :path [[0 -1] [0 -1] [1 0] [1 0] [0 1] [0 1] [-1 0] [-1 0]]})

(def animal-types
  {:chicken chicken
   :chicken2 chicken2
   :cow cow
   :pig pig})

(defn new-animal [atype]
  (FarmAnimal. (random-uuid) atype :inv 0 false true))

(defn add-animal-to-inventory [state atype]
  (assoc state :animals
         (conj (:animals state) (new-animal atype))))

(defn rotate-vector [v n]
  (loop [out v
         c 0]
    (if (< c n)
      (recur (concat (rest out) [(first out)]) (inc c))
      out)))

(defn walk-path
  ([animal] (walk-path (:path (:atype animal)) (:pos animal) (:ix animal)))
  ([path origin ix]
   (let [path (rotate-vector path ix)]
     (loop [[x y] origin
            out [[x y] (first path)]
            ix 0]
       (let [node (nth path ix)
             x (+ x (first node))
             y (+ y (second node))]
         (if (< ix (- (count path) 1))
           (recur [x y]
                  (apply concat [out [[x y] node]])
                  (inc ix))
           (mapv vec (partition 2 out))))))))

(defn can-place-animal-here [state animal pos]
  (let [existing-animal (first (filter #(= (:pos %) pos) (:animals state)))
        walked-path (walk-path (:path (:atype animal)) pos 0)
        max (- (:sz state) 1)
        out-of-bounds-tiles (filter (fn [[[x y]]] (or (< x 0)
                                                      (< y 0)
                                                      (> x max)
                                                      (> y max))) walked-path)]
    (js/console.log "walked-path" (clj->js walked-path))
    (and (not existing-animal)
         (= (count out-of-bounds-tiles) 0))))

(defn get-moving-animal [state]
  (when-let [moving-animal-id (:moving-animal state)]
    (when-let [out (first (filter #(= (:id %) moving-animal-id)
                                  (:animals state)))]
      out)))

(defn get-animal-by-id-indexed [state id]
  (first (keep-indexed #(when (= (:id %2) id) [%1 %2]) (:animals state))))

(defn place-animal [state pos]
  (let [id (:moving-animal state)
        [ix animal] (get-animal-by-id-indexed state id)
        is-in-inventory (= (:pos animal) :inv)
        is-valid-placement (can-place-animal-here state animal pos)
        can-place (and animal is-in-inventory is-valid-placement)]
    (if can-place
      (let [new-state (-> state
                          (assoc :moving-animal nil)
                          (assoc-in [:animals ix] (assoc animal :pos pos)))
            new-moveable-animals (filter #(= (:pos %) :inv) (:animals new-state))]
        (if (> (count new-moveable-animals) 0)
          (assoc new-state :moving-animal (:id (first new-moveable-animals)))
          new-state))
      state)))

(defn next-day [state]
  (let [day (inc (:day state))
        level (:level state)
        rounds (:rounds level)
        round (get rounds day)]
    (js/console.log "round" (clj->js round))
    (if round
      (-> state
          (assoc :day day)
          (assoc :animals
                 (vec (concat
                       (:animals state)
                       (mapv new-animal
                             (map animal-types round))))))
      (assoc state :win true))))

(defn active-animals [state]
  (filter #(not= :inv (:pos %)) (:animals state)))


(defn inv-animals [state]
  (filter #(= :inv (:pos %)) (:animals state)))

(defn animal-get-turn-path [animal]
  (let [pos (:pos animal)
        path (:path (:atype animal))
        path-ix (:ix animal)
        x (first pos)
        y (second pos)
        delta (nth path path-ix)
        dx (first delta)
        dy (second delta)
        new-pos [(+ x dx)
                 (+ y dy)]]
    [pos new-pos]))

(defn animal-move-along-path [animal path]
  (let [[sx sy] (first path)
        [ex ey] (last path)]
    (FarmAnimal. (:id animal)
                 (:atype animal)
                 [ex ey]
                 (mod (inc (:ix animal))
                      (count (:path (:atype animal))))
                 (if (< ex sx) true false)
                 false)))

(defn drop-nth [n coll]
  (keep-indexed #(when (not= %1 n) %2) coll))

(defn get-colliding-animals [animals paths]
  (let [final-positions (map last paths)
        reversed-paths (map reverse paths)]
    (keep-indexed
     (fn [ix a]
       (let [p (nth paths ix)
             final-pos (last p)
             not-me-final-positions (set (drop-nth ix final-positions))
             not-me-rev-paths (set (drop-nth ix reversed-paths))]
         (when (or
                ; Two animals have the same final position
                (some #(= % final-pos) not-me-final-positions)
                ; Two animals are "swapping" positions
                (some #(= % p) not-me-rev-paths))
           a)))
     animals)))

(defn simulate [state]
  (let [animals (active-animals state)
        paths (map animal-get-turn-path animals)
        collisions (get-colliding-animals animals paths)
        new-animals (vec (map-indexed #(animal-move-along-path %2 (nth paths %1)) animals))]
    (if (> (count collisions) 0)
      (-> state
          (assoc :game-over true)
          (assoc :collisions collisions)
          (assoc :crazy-animal (first collisions)))
      (-> state
          (assoc :animals new-animals)))))
