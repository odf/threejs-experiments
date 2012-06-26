(ns hello
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(defn light [{:keys [x y z]} color]
  (doto (THREE.PointLight. color)
    (-> .-position (.set x y z))))

(defn lambert [color]
  (THREE.MeshLambertMaterial. (js-obj "color" color)))

(defn sphere [radius segments rings]
  (THREE.SphereGeometry. radius segments rings))

(defn mesh [geometrie {:keys [x y z]} material]
  (doto (THREE.Mesh. geometrie material)
    (-> .-position (.set x y z))))

(def scene
  (doto (THREE.Scene.)
    (.add (mesh (sphere 50 16 16) { :x 0 :y 0 :z 0 } (lambert 0xCC2020)))
    (.add (mesh (sphere 20 16 16) { :x 80 :y 50 :z 0 } (lambert 0xCCCCCC)))
    (.add (light { :x 150 :y 300 :z 1000 } 0xFFFFFF))
    (.add (light { :x -150 :y 300 :z -1000 } 0x8080FF))))

(def viewport {:width 400 :height 300})

(def camera
  (let [view_angle 45
        aspect (/ (:width viewport) (:height viewport))
        near 0.1
        far 10000]
    (THREE.PerspectiveCamera. view_angle aspect near far)))

(def renderer
  (doto (THREE.WebGLRenderer.)
    (.setSize (:width viewport) (:height viewport))))

(.add scene camera)

(em/at js/document
       ["#container"] (em/append (.-domElement renderer)))

(js/animate (fn [] (js/render renderer scene camera)))

(js/alert "Hello from ClojureScript!")
