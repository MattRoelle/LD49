(ns app.game)

(defrecord FarmAnimal [id atype pos ix])
(def chicken {:name "Chicken" :path [[-1 0] [0 1] [1 0] [0 -1]]})

(defn simulate-animal [animal]
  (let [pos (:pos animal)
        path (:path (:atype animal))
        path-ix (:ix animal)
        x (first pos)
        y (second pos)
        delta (nth path path-ix)
        dx (first delta)
        dy (second delta)
        new-position [(+ x dx)
                      (+ y dy)]
        new-ix (mod (inc path-ix) (count path))]
    (FarmAnimal. (:id animal) (:atype animal) new-position new-ix)))

(defn simulate [state]
  (let [animals (:animals state)
        new-animals (map simulate-animal animals)]
    (assoc state :animals new-animals)))