(ns org.gavrog.cljs.threejs)

(defn- js-map [cljmap]
  (let [out js-obj]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn- v3 [[x y z]]
  (THREE.Vector3. x y z))

(defn set-position! [obj [x y z]]
  (-> obj .-position (.set x y z)))

(defn set-rotation! [obj [x y z]]
  (-> obj .-rotation (.set x y z)))

(defn set-name! [obj name]
  (-> obj .-name (set! name)))

(defn lambert [parameters]
  (THREE.MeshLambertMaterial. (js-map parameters)))

(defn phong [parameters]
  (THREE.MeshPhongMaterial. (js-map parameters)))

(defn sphere [radius segments rings]
  (THREE.SphereGeometry. radius segments rings))

(defn mesh [name position geometry material]
  (doto (THREE.Mesh. geometry material)
    (set-position! position)
    (set-name! name)))

(defn light [name position color]
  (doto (THREE.PointLight. color)
    (set-position! position)
    (set-name! name)))

(defn camera [name position {:keys [angle aspect near far lookAt]}]
  (doto (THREE.PerspectiveCamera. (or angle 25) (or aspect 1)
                                  (or near 0.1) (or far 10000))
    (set-position! position)
    (.lookAt (v3 (or lookAt [0 0 0])))
    (set-name! name)))

(defn group [name position & objs]
  (let [result (THREE.Object3D.)]
    (doseq [x objs] (.add result x))
    (set-name! result name)
    (set-position! result position)
    result))

(defn scene [& objs]
  (let [result (THREE.Scene.)]
    (doseq [x objs] (.add result x))
    result))

(defn renderer [width height]
  (doto (THREE.WebGLRenderer.)
    (.setSize width height)))
