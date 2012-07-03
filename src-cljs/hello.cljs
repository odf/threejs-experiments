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
  (apply + (map * u v)))

(defn- norm [v]
  (Math/sqrt (dot v v)))

(defn- cross [u v]
  (let [[u0 u1 u2] u
        [v0 v1 v2] v]
    [(- (* u1 v2) (* u2 v1))
     (- (* u2 v0) (* u0 v2))
     (- (* u0 v1) (* u1 v0))]))

(defn- scaled [f v]
  (map * (repeat f) v))

(defn- normalized [v]
  (scaled (/ 1 (norm v)) v))

(defn- make-stick
  ([p q radius segments]
     (let [n segments
           d (normalized (map - q p))
           u (cross d (if (> (dot d [1 0 0]) 0.9) [0 1 0] [1 0 0]))
           v (cross d u)
           a (/ (* 2 Math/PI) n)
           corner #(scaled radius (map + (scaled (Math/cos (* a %)) u)
                                      (scaled (Math/sin (* a %)) v)))
           section (map corner (range n))
           stick (THREE.Geometry.)]
       (doseq [[x y z] (map #(map + % p) section)]
         (-> stick .-vertices (.push (THREE.Vector3. x y z))))
       (doseq [[x y z] (map #(map + % q) section)]
         (-> stick .-vertices (.push (THREE.Vector3. x y z))))
       (doseq [i (range n) :let [j (-> i (+ 1) (mod n))]]
         (-> stick .-faces (.push (THREE.Face4. i j (+ j n) (+ i n)))))
       (.computeBoundingSphere stick)
       (.computeFaceNormals stick)
       (.computeVertexNormals stick)
       stick
       ))
  ([p q radius]
     (make-stick p q radius 8)))

(defn- ball-and-stick [positions edges]
  (let [ball-material (phong {:color 0xCC2020 :shininess 100})
        stick-material (phong {:color 0x2020CC :shininess 100})
        balls (map-values #(mesh (sphere 10 8 8) % ball-material) positions)
        stick (fn [[u v]] (make-stick (positions u) (positions v) 5))
        sticks (map #(mesh stick [0 0 0] stick-material) edges)]
    ))

(def ^{:private true} viewport {:width 400 :height 300})

(def ^{:private true} camera
  (let [view_angle 25
        aspect (/ (:width viewport) (:height viewport))
        near 0.1
        far 10000]
    (doto (THREE.PerspectiveCamera. view_angle aspect near far)
      (-> .-position (.set 0 0 350))
      (.lookAt (THREE.Vector3. 0 0 0))
      )))

(def ^{:private true} test-stick
  (mesh (make-stick [50 0 0] [60 70 10] 20) [0 0 0] (phong {:color 0x2020CC})))

(def ^{:private true} group
  (doto (THREE.Object3D.)
    (.add (mesh (sphere 50 16 16) [0 0 0]
                (phong {:color 0xCC2020})))
    (.add (mesh (sphere 20 16 16) [80 50 0]
                (phong {:color 0xCCCCCC :shininess 100})))
    (.add test-stick)
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
