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
           section (map corner (range n))]
       (t/geometry
        (concat (map #(map + % p) section) (map #(map + % q) section))
        (for [i (range n) :let [j (-> i (+ 1) (mod n))]]
          [i j (+ j n) (+ i n)]))))
  ([p q radius]
     (stick p q radius 8)))

(defn- ball-and-stick [name positions edges ball-material stick-material]
  (let [balls (for [[k p] positions]
                (t/mesh (pr-str k) p (t/sphere 10 8 8) ball-material))
        sticks (for [[u v] edges]
                 (t/mesh (pr-str [u v]) [0 0 0]
                         (stick (positions u) (positions v) 5) stick-material))]
    (apply t/group (concat [name [0 0 0]] balls sticks))))

(def ^{:private true} viewport {:width 400 :height 300})

(def ^{:private true} camera
  (let [{:keys [width height]} viewport]
    (t/camera "camera" [0 0 350] {:aspect (/ width height)})))

(def ^{:private true} group
  (t/group "group" [0 0 0]
           (t/mesh "center" [0 0 0] (t/sphere 50 16 16)
                   (t/phong {:color 0xFFDD40}))
           (ball-and-stick "graph"
                           {:--- [-50 -50 -50] :--+ [-50 -50  50]
                            :-+- [-50  50 -50] :-++ [-50  50  50]
                            :+-- [ 50 -50 -50] :+-+ [ 50 -50  50]
                            :++- [ 50  50 -50] :+++ [ 50  50  50]}
                           [[:--- :--+] [:-+- :-++] [:+-- :+-+] [:++- :+++]
                            [:--- :-+-] [:--+ :-++] [:+-- :++-] [:+-+ :+++]
                            [:--- :+--] [:--+ :+-+] [:-+- :++-] [:-++ :+++]]
                           (t/phong {:color 0xCC2020 :shininess 100})
                           (t/phong {:color 0x2020CC :shininess 100}))))

(def ^{:private true} scene
  (t/scene group
           (t/light "main" [150 300 1000] 0xCCCCCC)
           (t/light "fill" [-300 -100 1000] 0x444444)
           (t/light "back" [300 300 -1000] 0x8080FF)
           camera))

(def ^{:private true} renderer
  (let [{:keys [width height]} viewport]
    (t/renderer width height)))

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
