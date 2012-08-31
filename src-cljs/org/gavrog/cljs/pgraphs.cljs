;; This module deals with periodic graphs.
;;
;; A periodic graph is encoded by its quotient graph, which is a finite
;; multi-graph with vector-labelled edges.


;; (Obsolete description, taken from the old Python version:
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
;;   k-dimensional lattice Z^k.)

(ns org.gavrog.cljs.pgraphs
  (:require [org.gavrog.cljs.vectormath :as v]))

(defn map-values [f m] (reduce (fn [m [k v]] (assoc m k (f v))) m m))

(defn dimension [G] (G :dim))

(defn vertices [G] (G :verts))

(defn adjacent [G v] (or ((G :adjs) v) #{}))

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

(defn with-vertex [G v]
  (if (vertex? G v)
    G
    (let [{:keys [dim verts adjs]} G]
      {:dim dim
       :verts (conj verts v)
       :adjs (assoc adjs v #{})})))

(defn without-vertex [G v]
  (if-not (vertex? G v)
    G
    (let [{:keys [dim verts adjs]} G]
      {:dim dim
       :verts (disj verts v)
       :adjs (map-values #(reduce disj % (filter (fn [[w s]] (= v w)) %))
                         (dissoc adjs v))})))

(defn with-edge [G v w s]
  (if (edge? G v w s)
    G
    (let [{:keys [dim verts adjs]} G]
      {:dim dim
       :verts (conj verts v w)
       :adjs (assoc adjs
               v (conj (or (adjs v) #{}) [w s])
               w (conj (or (adjs w) #{}) [v (map - s)]))})))

(defn without-edge [G v w s]
  (if-not (edge? G v w s)
    G
    (let [{:keys [dim verts adjs]} G]
      {:dim dim
       :verts verts
       :adjs (assoc adjs
               v (disj (adjs v) [w s])
               w (disj (adjs w) [v (map - s)]))})))

(defn origin [G] (repeat (dimension G) 0))

(defn make-graph [dim & edge-specs]
  (reduce (fn [G [v w & s]] (with-edge G v w s))
          {:dim dim :verts #{} :adjs {}}
          (partition (+ 2 dim) edge-specs)))

(defn- next-level [graph prev curr]
  (let [tmp (transient #{})
        nxt (for [[v s] curr
                  w (neighbors graph v s)
                  :when (not (or (prev w) (curr w)))]
              w)]
    (doseq [v nxt] (conj! tmp v))
    (persistent! tmp)))

(defn- cs [graph previous current]
  (let [advance (next-level graph previous current)]
    (lazy-seq (cons (count current) (cs graph current advance)))))

(defn coordination-sequence [graph seed]
  (cs graph #{} (into #{} [[seed (origin graph)]])))

(defn- dfs [graph start]
  (let [stack (transient [start])
        shifts (transient {start (origin graph)})
        translations (transient #{})]
    (while (> (count stack) 0)
      (let [v (nth stack 0)]
        (pop! stack)
        (doseq [[w s] (adjacent graph v)]
          (if-let [t (get shifts w)]
            (conj! translations (map + s t (map - (get shifts v))))
            (do
              (conj! stack w)
              (assoc! shifts w (map - (get shifts v) s)))))))
    [(keys (persistent! shifts)) (persistent! translations)]))

(defn components [graph]
  (let [seen (transient #{})
        result (transient [])]
    (doseq [v (vertices graph)]
      (if-not (seen v)
        (let [[nodes translations] (dfs graph v)]
          (conj! result [nodes translations])
          (doseq [v nodes] (conj! seen v)))))
    (persistent! result)))
