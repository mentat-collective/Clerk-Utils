(ns mentat.clerk-utils
  "Utilities, TODO fill in."
  (:import (java.io FileNotFoundException)))

(defn- ns-loaded? [sym]
  (try (nil? (resolve sym))
       (catch FileNotFoundException _ false)))

;; ->clerk

(defmacro ->clerk [body]
  (when (ns-loaded? 'nextjournal.clerk)
    body))

;; ## Example Macro

(defmacro cljs [& exprs]
  (->clerk
   `(nextjournal.clerk/with-viewer
      {:transform-fn nextjournal.clerk/mark-presented
       :render-fn '(fn [_#]
                     (let [result# (do ~@exprs)]
                       (v/html
                        (if (vector? result#)
                          result#
                          [v/inspect result#]))))}
      {})))
