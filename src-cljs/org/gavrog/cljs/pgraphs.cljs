;; This module deals with periodic graphs.
;;
;; A periodic, undirected graph is given as a sequence of edges.
;;   
;; Edges look like (i,j,(a1,...,ak)), where
;;   - i,j are comparable vertex labels, usually positive integers
;;   - a1,...,ak are integers forming a vector in the integral lattice.
;;   
;; Conditions:
;; - no edge may occur twice with the same vector
;; - the resulting graph is connected
;; - the vectors all have the same dimension k and together span the
;;   k-dimensional lattice Z^k.

(ns org.gavrog.cljs.pgraphs
  (:require [org.gavrog.cljs.vectormath :as v]))

(defn map-values [f m] (reduce (fn [m [k v]] (assoc m k (f v))) m m))

(defprotocol IPGraph
  (dimension [G])
  (vertices [G])
  (adjacent [G v])
  (with-vertex [G v])
  (without-vertex [G v])
  (with-edge [G v w s])
  (without-edge [G v w s]))

(defn- reverse-edge? [v w s]
  (or (> v w)
      (and (= v w) (< s (repeat 0)))))

(defn vertex? [G v]
  ((vertices G) v))

(defn isolated? [G v]
  (and (vertex? G v)
       (empty? (adjacent G v))))

(defn edge? [G v w s]
  (and (vertex? G v)
       ((adjacent G v) [w s])))

(defn edges [G]
  (for [v (vertices G)
        [w s] (adjacent G v)
        :when (not (reverse-edge? v w s))]
    [v w s]))

(defn neighbors [G v s]
  (map (fn [[w t]] (vector w (map + s t))) (adjacent G v)))

(defn origin [G] (repeat (dimension G) 0))

(defrecord PGraph [dim verts adjs]
  IPGraph
  (dimension [G] dim)
  (vertices [G] verts)
  (adjacent [G v] (or (adjs v) #{}))
  (with-vertex [G v]
    (if (vertex? G v)
      G
      (PGraph. dim
               (conj verts v)
               (assoc adjs v #{}))))
  (without-vertex [G v]
    (if-not (vertex? G v)
      G
      (PGraph. dim
              (disj verts v)
              (map-values #(reduce disj % (filter (fn [[w s]] (= v w)) %))
                          (dissoc adjs v)))))
  (with-edge [G v w s]
    (if (edge? G v w s)
      G
      (PGraph. dim
              (conj verts v w)
              (assoc adjs
                v (conj (or (adjs v) #{}) [w s])
                w (conj (or (adjs w) #{}) [v (map - s)])))))
  (without-edge [G v w s]
    (if-not (edge? G v w s)
      G
      (PGraph. dim
              verts
              (assoc adjs
                v (disj (adjs v) [w s])
                w (disj (adjs w) [v (map - s)]))))))

(defn make-graph [dim & edge-specs]
  (reduce (fn [G [v w & s]] (with-edge G v w s))
          (PGraph. dim #{} {})
          (partition (+ 2 dim) edge-specs)))

(defn coordination-sequence [graph seed]
  (letfn [(step [previous current]
            (let [advance (into #{}
                                (for [[v s] current
                                      x (neighbors graph v s)
                                      :when (not (or (previous x) (current x)))]
                                  x))]
              (lazy-seq (cons (count current) (step current advance)))))]
    (step #{} (into #{} [[seed (origin graph)]]))))
