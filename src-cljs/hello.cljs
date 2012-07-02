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

(defn- map-values [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn- dot [u v]
  (into [] (apply + (map * u v))))

(defn- cross [u v]
  [(- (* (u 1) (v 2)) (* (u 2) (v 1)))
   (- (* (u 2) (v 0)) (* (u 0) (v 2)))
   (- (* (u 0) (v 1)) (* (u 1) (v 0)))])

(defn- normalized [v]
  (let [norm (Math/sqrt (dot v v))]
    (into [] (map #(/ % norm) v))))

(defn- make-stick
  ([p q segments]
     (let [d (normalized (map - q p))
           u (cross d (if (> (dot d [1 0 0]) 0.9) [0 1 0] [1 0 0]))
           v (cross d u)
           a (/ (* 2 Math/PI) segment)
           lc (fn [nu v mu w] (into [] (map #(+ (* nu %1) (* mu %2)) v w)))
           section (map #(lc (Math/cos (* a %)) u (Math/sin (* a %)) v)
                        (range segments))]))
  ([p q]
     (make-stick p q 8)))

(defn- ball-and-stick [positions edges]
  (let [ball-material (phong {:color 0xCC2020 :shininess 100})
        balls (map-values #(mesh (sphere 10 8 8) % ball-material) positions)
        sticks (map (fn [[u v]] (make-stick (positions u) (positions v))) edges)]
    ))

(def ^{:private true} viewport {:width 400 :height 300})

(def ^{:private true} camera
  (let [view_angle 45
        aspect (/ (:width viewport) (:height viewport))
        near 0.1
        far 10000]
    (doto (THREE.PerspectiveCamera. view_angle aspect near far)
      (-> .-position (.set 0 0 200))
      (.lookAt (THREE.Vector3. 0 0 0))
      )))

(def ^{:private true} test
  (doto (mesh (THREE.CylinderGeometry. 5 5 50 8 1 false)
              [60 0 0] (phong {:color 0x2020CC}))
    (.lookAt (THREE.Vector3. 60 60 0))))

(def ^{:private true} group
  (doto (THREE.Object3D.)
    (.add (mesh (sphere 50 16 16) [0 0 0]
                (phong {:color 0xCC2020})))
    (.add (mesh (sphere 20 16 16) [80 50 0]
                (phong {:color 0xCCCCCC :shininess 100})))
    (.add test)
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
