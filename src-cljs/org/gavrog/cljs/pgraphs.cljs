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
  :require [org.gavrog.cljs.vectormath :as v])

;; Variations of filter and map that don't create new collections.

(defn filter-set [f s] (reduce disj s (filter #(not (f %)) s)))

(defn map-values [f m]
  (reduce (fn [m [k v]] (let [fv (f v)] (if (= v fv) m (assoc m k fv)))) m m))


;; An implementation of periodic graphs as a persistent data structure.

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
  (shift [G v w] (if (< v w)
                   (or (shifts [v w])
                       (apply vector (repeat dim 0)))
                   (map - (shift G w v)))))

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
