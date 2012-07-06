(ns org.gavrog.cljs.vectormath)

(defn dot [u v]
  (apply + (map * u v)))

(defn norm [v]
  (Math/sqrt (dot v v)))

(defn cross [u v]
  (let [[u0 u1 u2] u
        [v0 v1 v2] v]
    [(- (* u1 v2) (* u2 v1))
     (- (* u2 v0) (* u0 v2))
     (- (* u0 v1) (* u1 v0))]))

(defn scaled [f v]
  (map * (repeat f) v))

(defn normalized [v]
  (scaled (/ 1 (norm v)) v))
