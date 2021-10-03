(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [app.levels :as l]
            [app.game :as g]
            [app.presentation :as p]))


(defn mount-app-root []
  (rd/render [p/root]
            (.getElementById js/document "app-root")))

(mount-app-root)