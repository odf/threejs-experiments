;; This module deals with periodic graphs.
;;
;; A periodic, undirected graph is given as a sequence of edges.
;;   
;; Edges look like (i,j,(a1,...,ak)), where
;;   - i,j are natural numbers (excluding 0) representing vertices
;;   - a1,...,ak are integers forming a vector in the integral lattice.
;;   
;; Conditions:
;; - i <= j (forced direction)
;; - special case: if i == j, then v must not be lexicographically smaller
;;   than the zero vector
;; - no edge may occur twice
;; - the resulting graph is connected
;; - the vectors all have the same dimension k and together span the
;;   k-dimensional lattice Z^k.

(ns org.gavrog.cljs.pgraphs
  (:require [org.gavrog.cljs.vectormath :as v]))

(defn map-values [f m] (reduce (fn [m [k v]] (assoc m k (f v))) m m))

;; TODO we need to model adjacencies as vertex-vector pairs.

(defprotocol IPGraph
  (dimension [G])
  (vertices [G])
  (adjacent [G v])
  (shift [G v w])
  (with-vertex [G v])
  (without-vertex [G v])
  (with-edge [G v w s])
  (without-edge [G v w]))

(defrecord PGraph [dim verts adjs shifts]
  IPGraph
  (dimension [G] dim)
  (vertices [G] verts)
  (adjacent [G v] (or (adjs v) #{}))
  (shift [G v w] (if (> v w)
                   (map - (shift G w v))
                   (let [s (or (shifts [v w])
                               (apply vector (repeat dim 0)))]
                     (if (and (= v w)
                              (< s (repeat 0)))
                       (map - s)
                       s))))
  (with-vertex [G v]
    (if (verts v)
      G
      (Graph. dim (conj verts v) adjs shifts)))
  (without-vertex [G v]
    (if-not (verts v)
      G
      (Graph. dim
              (disj verts v)
              (map-values #(reduce disj % (filter (partial = v) %))
                          (dissoc adjs v))
              (reduce dissoc shifts (filter #((set %) v) (keys shifts))))))
  (with-edge [G v w s]
    (cond
     (> v w)
     (with-edge G w v (map - s))
     (and (verts v) ((adjs v) w) (= s (shifts [v w])))
     G
     :else
     ())))

(defn vertex? [G v]
  ((vertices G) v))

(defn isolated? [G v]
  (and (vertex? G v)
       (empty? (adjacent G v))))

(defn edge?
  ([G v w]
     (and (vertex? G v)
          ((adjacent G v) w)))
  ([G v w s]
     (and (edge? G v w)
          (= s (shift G v w)))))

(defn edges [G]
  (for [v (vertices G)
        w (adjacent G v)
        :when (< v w)]
    [v w (shift G v w)]))
