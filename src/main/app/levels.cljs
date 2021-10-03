(ns app.levels)

(def level-1 {:name "Level 1"
              :sz 6
              :rounds [[:chicken :chicken2 :chicken]
                       [:chicken]]})

(def level-2 {:name "Level 2"
              :sz 12
              :rounds [[:chicken]
                       [:chicken]
                       [:chicken]]})

(def levels [level-1
             level-2])