(ns app.game)

(defrecord FarmAnimal [id atype pos ix])
(def chicken {:name "Chicken" :path [[-1 0] [0 1] [1 0] [0 -1]]})
(def chicken2 {:name "Chicken" :path [[-1 0] [-1 0] [-1 0] [1 0] [1 0] [1 0]]})

(def animal-types
  {:chicken chicken})

(defn new-animal [atype]
  (FarmAnimal. (random-uuid) atype :inv 0))

(defn add-animal-to-inventory [state atype]
  (assoc state :animals
         (conj (:animals state) (new-animal atype))))

(defn can-place-animal-here [state animal pos]
  (let [existing-animal (first (filter #(= (:pos %) pos) (:animals state)))]
    (not existing-animal)))

(defn get-animal-by-id-indexed [state id]
  (first (keep-indexed #(when (= (:id %2) id) [%1 %2]) (:animals state))))

(defn place-animal [state pos]
  (let [id (:moving-animal state)
        [ix animal] (get-animal-by-id-indexed state id)
        is-in-inventory (= (:pos animal) :inv)
        is-valid-placement (can-place-animal-here state animal pos)
        can-place (and animal is-in-inventory is-valid-placement)]
    (if can-place
      (-> state
          (assoc :moving-animal nil)
          (assoc-in [:animals ix] (assoc animal :pos pos)))
      state)))

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
  (FarmAnimal. (:id animal)
               (:atype animal)
               (last path)
               (mod (inc (:ix animal))
                    (count (:path (:atype animal))))))

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
        ;;  (js/console.log #js{:animal (clj->js a)
        ;;                      :end-pos (clj->js final-pos)
        ;;                      :others (clj->js not-me-final-positions)})
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
        new-animals (map-indexed #(animal-move-along-path %2 (nth paths %1)) animals)]
    (js/console.log (clj->js collisions))
    (assoc state :animals (vec new-animals))))
