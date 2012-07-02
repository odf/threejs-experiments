(ns hello
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(defn- js-map [cljmap]
  (let [out js-obj]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn- set-position! [obj [x y z]]
  (-> obj .-position (.set x y z)))

(defn- set-rotation! [obj [x y z]]
  (-> obj .-rotation (.set x y z)))

(defn- light [position color]
  (doto (THREE.PointLight. color) (set-position! position)))

(defn- lambert [parameters]
  (THREE.MeshLambertMaterial. (js-map parameters)))

(defn- phong [parameters]
  (THREE.MeshPhongMaterial. (js-map parameters)))

(defn- sphere [radius segments rings]
  (THREE.SphereGeometry. radius segments rings))

(defn- mesh [geometrie position material]
  (doto (THREE.Mesh. geometrie material) (set-position! position)))

(def viewport {:width 400 :height 300})

(def ^{:private true} camera
  (let [view_angle 45
        aspect (/ (:width viewport) (:height viewport))
        near 0.1
        far 10000]
    (doto (THREE.PerspectiveCamera. view_angle aspect near far)
      (-> .-position (.set 0 0 200))
      (.lookAt (THREE.Vector3. 0 0 0))
      )))

(def ^{:private true} group
  (doto (THREE.Object3D.)
    (.add (mesh (sphere 50 16 16) [0 0 0]
                (phong {:color 0xCC2020})))
    (.add (mesh (sphere 20 16 16) [80 50 0]
                (phong {:color 0xCCCCCC :shininess 100})))
    ))

(def ^{:private true} scene
  (doto (THREE.Scene.)
    (.add group)
    (.add (light [150 300 1000] 0xFFFFFF))
    (.add (light [-150 300 -1000] 0x8080FF))
    (.add camera)))

(def ^{:private true} renderer
  (doto (THREE.WebGLRenderer.)
    (.setSize (:width viewport) (:height viewport))))

(em/at js/document ["#container"] (em/append (.-domElement renderer)))

(defn- render []
  (let [timer (* (.now js/Date) 0.0001)]
    (set-rotation! group [0 timer 0])
    (.render renderer scene camera)))

(defn- animate []
  (.requestAnimationFrame js/window animate)
  (render))

(.log js/console "Starting animation")
(animate)
