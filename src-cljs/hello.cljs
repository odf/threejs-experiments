(ns hello
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(def viewport {:width 400 :height 300})

(defn set-position! [obj [x y z]]
  (-> obj .-position (.set x y z)))

(defn light [position color]
  (doto (THREE.PointLight. color) (set-position! position)))

(defn lambert [color]
  (THREE.MeshLambertMaterial. (js-obj "color" color)))

(defn sphere [radius segments rings]
  (THREE.SphereGeometry. radius segments rings))

(defn mesh [geometrie position material]
  (doto (THREE.Mesh. geometrie material) (set-position! position)))

(def camera
  (let [view_angle 45
        aspect (/ (:width viewport) (:height viewport))
        near 0.1
        far 10000]
    (THREE.PerspectiveCamera. view_angle aspect near far)))

(def scene
  (doto (THREE.Scene.)
    (.add (mesh (sphere 50 16 16) [0 0 0] (lambert 0xCC2020)))
    (.add (mesh (sphere 20 16 16) [80 50 0] (lambert 0xCCCCCC)))
    (.add (light [150 300 1000] 0xFFFFFF))
    (.add (light [-150 300 -1000] 0x8080FF))
    (.add camera)))

(def renderer
  (doto (THREE.WebGLRenderer.)
    (.setSize (:width viewport) (:height viewport))))

(em/at js/document ["#container"] (em/append (.-domElement renderer)))

(defn render []
  (let [timer (* (.now js/Date) 0.0001)]
    (doto camera
      (-> .-position (.set (* 200 (Math/cos timer)) 0 (* 200 (Math/sin timer))))
      (.lookAt (.-position scene)))
    (doseq [object (.-children scene)]
      (set! (.. object -rotation -x) (+ (.. object -rotation -x) 0.01))
      (set! (.. object -rotation -y) (+ (.. object -rotation -y) 0.005)))
    (.render renderer scene camera)))

(defn animate []
  (.requestAnimationFrame js/window animate)
  (render))

(.log js/console "Starting animation")
(animate)
