(ns app.game)

(defrecord FarmAnimal [id atype pos ix])
(def chicken {:name "Chicken" :path [[-1 0] [0 1] [1 0] [0 -1]]})

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

;; (defn get-collisions [animals paths]
;;   (map-indexed
;;    (fn [path]
;;      (some (set path)))
;;    paths))

(defn simulate [state]
  (let [animals (:animals state)
        paths (map animal-get-turn-path animals)
        collisions ()
        new-animals (map-indexed #(animal-move-along-path %2 (nth paths %1)) animals)]
    (assoc state :animals new-animals)))
