(ns mentat.clerk-utils.pattern
  (:require [clojure.string :as-alias s]
            [clojure.set :as cs]))

(defn map-kv
  "Returns a map of identical type and key set to `m`, with each value `v`
  transformed by the supplied function `vf` into `(vf v)` and each key `k`
  transformed into `(kf k)`.

  In the 2-arity case, only values are transformed."
  ([vf m]
   (map-kv identity vf m))
  ([kf vf m]
   (persistent!
    (reduce-kv (fn [acc k v]
                 (assoc! acc (kf k) (vf v)))
               (transient (empty m))
               m))))

(defn unquote?
  "Returns true if `form` is a form that should be included with no quoting into
  the returned form, false otherwise."
  [form]
  (and (sequential? form)
       (= (first form)
          'clojure.core/unquote)))

(defn unquote-splice?
  "Returns true if `form` is a sequence form that should be spliced directly into
  the returned form, false otherwise."
  [form]
  (and (sequential? form)
       (= (first form)
          'clojure.core/unquote-splicing)))

(defn unquoted-form
  "Given a `form` that responds `true` to [[unquote?]] or [[unquote-splice?]],
  returns the form from that form."
  [form]
  (second form))

(defn splice-reduce
  "Helper function for reducing over a sequence that might contain forms that need
  to be spliced into the resulting sequence. This is a sort of helper for a
  guarded `mapcat`.

  Takes a sequence `xs` and mapping function `f` and returns a sequence of
  sequences that, if concatenated together, would be identical to

  ```clojure
  (map f xs)
  ```

  Except that any `x` such that `(unquote-splice? x)` returns true would have
  its sequential value `x` spliced into the result instead of `(f x)`."
  [f xs]
  (let [[acc pending] (reduce
                       (fn [[acc pending] x]
                         (if (unquote-splice? x)
                           (let [form (unquoted-form x)]
                             (if (empty? pending)
                               [(conj acc form) []]
                               [(conj acc pending form) []]))
                           [acc (conj pending (f x))]))
                       [[] []]
                       xs)]
    (if (empty? pending)
      acc
      (conj acc pending))))

(defn compile-sym [aliases form]
  (list
   'quote
   (if-let [ns (namespace form)]
     (if-let [full-ns (aliases (symbol ns))]
       (symbol (str full-ns) (name form))
       form)
     form)))

(defn compile-form
  [aliases skel]
  (letfn [(compile-sequential [xs]
            (let [acc (splice-reduce compile xs)]
              (cond (empty? acc) ()
                    (= 1 (count acc)) (first acc)
                    :else `(concat ~@acc))))

          (compile [form]
            (cond (symbol? form)
                  (compile-sym aliases form)

                  (unquote? form)
                  (unquoted-form form)

                  (unquote-splice? form)
                  (into [] (unquoted-form form))

                  (map? form)
                  (map-kv compile compile form)

                  (vector? form)
                  `(vec ~(compile-sequential form))

                  (set? form)
                  `(set ~(compile-sequential form))

                  (sequential? form)
                  (if (empty? form)
                    form
                    `(seq ~(compile-sequential form)))

                  :else form))]
    (compile skel)))

(defmacro q [form]
  (let [alias-m (ns-aliases *ns*)]
    (compile-form alias-m form)))
