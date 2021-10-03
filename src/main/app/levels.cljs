(ns app.levels)

(def tutorial {:name "Tutorial"
              :sz 3
              :rounds [[:chicken]
                       [:chicken]]})

(def hen-house {:name "The Hen House"
              :sz 4
              :rounds [[:chicken :chicken2]
                       [:chicken2]
                       [:chicken]]})

(def scramble {:name "Beakfast scramble"
                :sz 4
                :rounds [[:pig :chicken]
                         [:chicken2 :chicken]
                         [:chicken]]})

(def dairy-air {:name "Dairy air"
              :sz 5
              :rounds [[:cow]
                       [:cow]
                       [:cow]]})

(def playtime {:name "Playtime"
              :sz 5
              :rounds [[:pig :cow] [:chicken] [:chicken2]]})

(def bacon-and-eggs {:name "Bacon & Eggs"
                  :sz 5
                  :rounds [[:pig :pig]
                           [:chicken :chicken]
                           [:chicken :pig]]})

(def clustercluck {:name "Clustercluck"
                   :sz 5
                   :rounds [[:chicken :chicken :chicken :chicken]
                            [:chicken :chicken :chicken]
                            [:chicken]
                            [:chicken]]})
(def milk-maids {:name "Milk Maids"
                   :sz 8
                   :rounds [[:cow :cow :cow]
                            [:cow :cow :cow]
                            [:cow :cow :cow]
                            [:cow]]})

(def what-the-cluck {:name "What The Cluck"
                 :sz 8
                 :rounds [[:chicken :chicken :chicken :chicken]
                          [:chicken :chicken :chicken :chicken]
                          [:chicken :chicken :chicken :chicken]
                          [:chicken :chicken :chicken :chicken]
                          [:chicken :chicken :chicken :chicken]
                          [:chicken :chicken :chicken]]})

(def crowd-control {:name "Crowd Control"
                    :sz 6
                    :rounds [[:chicken :chicken :chicken2 :pig :pig]
                             [:chicken :cow]
                             [:chicken]]})

(def levels [tutorial
             hen-house
             scramble
             dairy-air
             bacon-and-eggs
             playtime        
             clustercluck
             milk-maids
             what-the-cluck
             crowd-control])