(ns hello
  (:require [enfocus.core :as ef]
            [clojure.browser.repl :as repl])
  (:require-macros [enfocus.macros :as em]))

(repl/connect "http://localhost:9000/repl")

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

(defn- stick
  ([p q radius segments]
     (let [n segments
           d (normalized (map - q p))
           u (cross d (if (> (dot d [1 0 0]) 0.9) [0 1 0] [1 0 0]))
           v (cross d u)
           a (-> Math/PI (* 2) (/ n))
           corner #(let [x (* a %), c (Math/cos x), s (Math/sin x)]
                     (scaled radius (map + (scaled c u) (scaled s v))))
           section (map corner (range n))
           geometry (THREE.Geometry.)]
       (doseq [[x y z] (map #(map + % p) section)]
         (-> geometry .-vertices (.push (THREE.Vector3. x y z))))
       (doseq [[x y z] (map #(map + % q) section)]
         (-> geometry .-vertices (.push (THREE.Vector3. x y z))))
       (doseq [i (range n) :let [j (-> i (+ 1) (mod n))]]
         (-> geometry .-faces (.push (THREE.Face4. i j (+ j n) (+ i n)))))
       (.computeBoundingSphere geometry)
       (.computeFaceNormals geometry)
       (.computeVertexNormals geometry)
       geometry
       ))
  ([p q radius]
     (stick p q radius 8)))

(defn- ball-and-stick [positions edges]
  (let [red (phong {:color 0xCC2020 :shininess 100})
        blue (phong {:color 0x2020CC :shininess 100})
        group (THREE.Object3D.)]
    (doseq [[k p] positions]
      (.add group (mesh (sphere 10 8 8) p red)))
    (doseq [[u v] edges]
      (.add group (mesh (stick (positions u) (positions v) 5) [0 0 0] blue)))
    group
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

(def ^{:private true} test-graph
  (ball-and-stick {:--- [-50 -50 -50]
                   :--+ [-50 -50  50]
                   :-+- [-50  50 -50]
                   :-++ [-50  50  50]
                   :+-- [ 50 -50 -50]
                   :+-+ [ 50 -50  50]
                   :++- [ 50  50 -50]
                   :+++ [ 50  50  50]}
                  [[:--- :--+]
                   [:-+- :-++]
                   [:+-- :+-+]
                   [:++- :+++]
                   [:--- :-+-]
                   [:--+ :-++]
                   [:+-- :++-]
                   [:+-+ :+++]
                   [:--- :+--]
                   [:--+ :+-+]
                   [:-+- :++-]
                   [:-++ :+++]
                   ]))

(def ^{:private true} group
  (doto (THREE.Object3D.)
    (.add (mesh (sphere 50 16 16) [0 0 0] (phong {:color 0xFFDD40})))
    (.add test-graph)
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
