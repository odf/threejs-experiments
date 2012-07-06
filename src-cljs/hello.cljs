(ns hello
  (:require [org.gavrog.cljs.vectormath :as v]
            [org.gavrog.cljs.threejs :as t]
            [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(defn- stick
  ([p q radius segments]
     (let [n segments
           d (v/normalized (map - q p))
           u (v/cross d (if (> (v/dot d [1 0 0]) 0.9) [0 1 0] [1 0 0]))
           v (v/cross d u)
           a (-> Math/PI (* 2) (/ n))
           corner #(let [x (* a %), c (Math/cos x), s (Math/sin x)]
                     (v/scaled radius (map + (v/scaled c u) (v/scaled s v))))
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

(defn- ball-and-stick [name positions edges]
  (let [red (t/phong {:color 0xCC2020 :shininess 100})
        blue (t/phong {:color 0x2020CC :shininess 100})
        group (THREE.Object3D.)]
    (doseq [[k p] positions]
      (.add group (t/mesh (pr-str k) (t/sphere 10 8 8) p red)))
    (doseq [[u v] edges]
      (.add group (t/mesh (pr-str [u v]) (stick (positions u) (positions v) 5)
                          [0 0 0] blue)))
    (-> group .-name (set! name))
    group
    ))

(def ^{:private true} viewport {:width 400 :height 300})

(def ^{:private true} camera
  (let [{:keys [width height]} viewport]
    (t/camera "camera" [0 0 350] {:aspect (/ width height)})))

(def ^{:private true} test-graph
  (ball-and-stick "graph"
                  {:--- [-50 -50 -50]
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
    (.add (t/mesh "center" (t/sphere 50 16 16) [0 0 0]
                  (t/phong {:color 0xFFDD40})))
    (.add test-graph)
    (-> .-name (set! "group"))
    ))

(def ^{:private true} scene
  (doto (THREE.Scene.)
    (.add group)
    (.add (t/light "main" [150 300 1000] 0xCCCCCC))
    (.add (t/light "fill" [-300 -100 1000] 0x444444))
    (.add (t/light "back" [300 300 -1000] 0x8080FF))
    (.add camera)))

(def ^{:private true} renderer
  (doto (THREE.WebGLRenderer.)
    (.setSize (:width viewport) (:height viewport))))

(em/at js/document ["#container"] (em/append (.-domElement renderer)))

(defn- render []
  (let [timer (* (.now js/Date) 0.0001)]
    (t/set-rotation! group [0 timer 0])
    (.render renderer scene camera)))

(defn- animate []
  (.requestAnimationFrame js/window animate)
  (render))

(.log js/console "Starting animation")
(animate)
