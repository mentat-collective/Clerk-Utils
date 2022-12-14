(ns mentat.clerk-utils
  "Utilities for writing docs and working with Clerk in a project that may or may
  not have Clerk on the classpath.

  Expect this namespace to fission off into more specific namespaces as the
  catalogue of functions grows."
  (:import (java.io FileNotFoundException)))

(defn- ns-present?
  "Returns true if `sym` corresponds to a namespace present on the classpath and
  available for loading, false otherwise.

  NOTE that calling [[ns-present?]] will `require` the namespace as a
  side-effect."
  [sym]
  (try (nil? (require sym))
       (catch FileNotFoundException _ false)))

(defn- try-resolve
  "Takes a namespaced symbol `sym` and delegates to
  `clojure.core/requiring-resolve` if its namespace is present on the classpath,
  returns `nil` otherwise.

  NOTE that calling [[try-resolve]] will `require` the `sym`'s namespace as a
  side-effect."
  [sym]
  (try (requiring-resolve sym)
       (catch FileNotFoundException _ nil)))

;; ## Visibility Macros

(defmacro ->clerk
  "If `nextjournal.clerk` is present on the classpath, acts as `do` for all
  supplied code forms in `body`. Else, acts like `comment` and elides its
  arguments.

  Like `comment`, body must still be written in well-formed Clojure that can be
  parsed by the reader."
  [& body]
  (when (ns-present? 'nextjournal.clerk)
    `(do ~@body)))

(defmacro ->clerk-only
  "If `nextjournal.clerk` is present on the classpath AND Clerk is evaluating
  `body`, acts as `do` for all supplied code forms in `body`. Else, acts like
  `comment` and elides its arguments.

  Use this macro to supply examples and forms that should render in a Clerk
  environment but not affect normal Clojure evaluation in any way."
  ([& body]
   (when (some-> (try-resolve
                  'nextjournal.clerk.config/*in-clerk*)
                 deref)
     `(do ~@body))))

;; ## Example Macro

(defmacro cljs
  "Returns a form that executes all `exprs` in Clerk's SCI environment and renders
  the final form. If the final form evaluates to a vector, the vector is
  interpreted as a Reagent component.

  Else, the form is presented with `[v/inspect form]`. (To present a vector,
  manually wrap the final form in `[v/inspect ,,,]`.)

  If `nextjournal.clerk` is not present on the classpath, acts as `comment`.
  See [[->clerk]] for more detail."
  [& exprs]
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
