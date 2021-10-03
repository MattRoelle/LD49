(ns app.levels)

(def level-1 {:name "Level 1"
              :sz 4
              :rounds [[:chicken]
                       [:chicken]
                       [:chicken]]})

(def level-2 {:name "Level 2"
              :sz 4
              :rounds [[:chicken :chicken2]
                       [:chicken2]
                       [:chicken]]})

(def level-3 {:name "Level 3"
              :sz 5
              :rounds [[:cow :chicken2]
                       [:chicken]
                       [:chicken]]})

(def level-4 {:name "Level 4"
              :sz 5
              :rounds [[:cow :chicken2 :chicken]
                       [:chicken :chicken2]
                       [:chicken]]})

(def levels [level-1
             level-2
             level-3
             level-4])