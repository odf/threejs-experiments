(ns hello
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(def viewport (js-obj "width" 400 "height" 300))
(def camera (js/camera viewport))
(def scene (js/scene))
(def renderer (js/renderer viewport))
(defn animate []
  (do
    (js/requestAnimationFrame hello.animate)
    (js/render renderer scene camera)))

(.add scene camera)

(em/at js/document
       ["#container"] (em/append (.-domElement renderer)))

(animate)

(js/alert "Hello from ClojureScript!")
