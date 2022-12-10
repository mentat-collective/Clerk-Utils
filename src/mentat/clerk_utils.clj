(ns mentat.clerk-utils
  (:require [nextjournal.clerk :as clerk]))

;; ## Example Macro

(defmacro cljs [& exprs]
  `(clerk/with-viewer
     {:transform-fn clerk/mark-presented
      :render-fn '(fn [_#]
                    (let [result# (do ~@exprs)]
                      (v/html
                       (if (and (vector? result#)
                                (not (:inspect (meta result#))))
                         result#
                         [v/inspect result#]))))}
     {}))
