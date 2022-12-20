(ns mentat.clerk-utils.show
  "Show utilities for Clerk."
  (:require [applied-science.js-interop :as j]
            [clojure.walk :as walk]
            [nextjournal.clerk #?(:clj :as :cljs :as-alias) clerk]
            #?@(:cljs [nextjournal.clerk.static-app
                       nextjournal.viewer.notebook
                       nextjournal.clerk.render]))
  #?(:cljs
     (:require-macros mentat.clerk-utils.show)))

;; TODO this is going to need some behavior on the cljs side!! I think nothing,
;; since that is already taken care of?

(defmacro show-sci
  "Returns a form that executes all `exprs` in Clerk's SCI environment and renders
  the final form. If the final form evaluates to a vector, the vector is
  interpreted as a Reagent component.

  Else, the form is presented with `[v/inspect form]`. (To present a vector,
  manually wrap the final form in `[v/inspect ,,,]`.)"
  [& exprs]
  `(clerk/with-viewer
     {:transform-fn clerk/mark-presented
      :render-fn '(fn [_#]
                    (let [result# (do ~@exprs)]
                      (v/html
                       (if (vector? result#)
                         result#
                         [v/inspect result#]))))}
     {}))

;; ## Clerk ClojureScript/Reagent viewer
;;
;; (for using compiled ClojureScript in a notebook)

(defn- stable-hash-form
  "Replaces gensyms and regular expressions with stable symbols for consistent
  hashing."
  [form]
  (let [!counter (atom 0)
        !syms (atom {})]
    (walk/postwalk
     (fn [x]
       (cond #?(:cljs (regexp? x)
                :clj  (instance? java.util.regex.Pattern x))
             (symbol (str "stable-regexp-" (swap! !counter inc)))
             (and (symbol? x)
                  (not (namespace x)))
             (or (@!syms x)
                 (let [y (symbol (str "stable-symbol-" (swap! !counter inc)))]
                   (swap! !syms assoc x y)
                   y))
             :else x)) form)))

(def ^:no-doc stable-hash
  (comp hash stable-hash-form))

;; TODO we can really win if we can figure out some way to call this code very
;; naturally from SCI.

(defmacro show-cljs
  "Evaluate expressions in ClojureScript instead of Clojure.

  Result is treated as hiccup if it is a vector (unless tagged with ^:inspect),
  otherwise passed to Clerk's `inspect`."
  [& exprs]
  (let [fn-name (str *ns* "-" (stable-hash exprs))]
    (if (:ns &env)
      ;; in ClojureScript, define a function
      `(let [f# (fn [] ~@exprs)]
         (j/update-in! ~'js/window [:clerk-cljs ~fn-name]
                       (fn [x#]
                         (cond (not x#) (reagent.core/atom {:f f#})
                               (:loading? @x#) (doto x# (reset! {:f f#}))
                               :else x#))))
      ;; in Clojure, return a map with a reference to the fully qualified sym
      `(clerk/with-viewer
         {:transform-fn clerk/mark-presented
          :render-fn
          '(fn render-var []
             ;; ensure that a reagent atom exists for this fn
             (applied-science.js-interop/update-in!
              js/window
              [:clerk-cljs ~fn-name]
              (fn [x] (or x (reagent.core/atom {:loading? true}))))
             (let [res @(j/get-in js/window [:clerk-cljs ~fn-name])]
               (if (:loading? res)
                 [:div.my-2 {:style {:color "rgba(0,0,0,0.5)"}} "Loading..."]
                 (let [result (try ((:f res))
                                   (catch js/Error e
                                     (js/console.error e)
                                     [nextjournal.clerk.render/error-view e]))]
                   (if (and (vector? result)
                            (not (:inspect (meta result))))
                     [:div.my-1 result]
                     [nextjournal.clerk.render/inspect result])))))}
         nil))))
