(ns repro.core
  (:require [fulcro.client :as fc]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.dom :as dom]))

(defonce app (atom nil))

(defsc Root
  [this props]
  {}
  (dom/div "hi"))

(defn start
  []
  (reset! app (fc/mount @app Root "app")))

(defn ^:export init []
  (reset! app (fc/make-fulcro-client))
  (start))
