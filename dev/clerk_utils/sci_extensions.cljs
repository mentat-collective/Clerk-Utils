(ns clerk-utils.sci-extensions
  (:require [clerk-utils.custom]
            [mentat.clerk-utils.sci]
            ["react" :as react]
            [sci.ctx-store]
            [sci.core :as sci]))

;; ## Custom ClojureScript

;; This form creates a "lives-within-SCI" version of the `clerk-utils.custom`
;; namespace by copying all public vars.
(def custom-namespace
  (sci/copy-ns clerk-utils.custom
               (sci/create-ns 'clerk-utils.custom)))

;; This next form mutates SCI's default environment, merging in your custom code
;; on top of what Clerk has already configured.
(sci.ctx-store/swap-ctx!
 sci/merge-opts
 {;; Use `:classes` to expose JavaScript classes that you'd like to use in your
  ;; viewer code. `Math/sin` etc will work with this entry:
  :classes    {'Math  js/Math}

  ;; Adding an entry to this map is equivalent to adding an entry like
  ;; `(:require [clerk-utils.custom])` to a Clojure namespace.
  :namespaces {'clerk-utils.custom custom-namespace}

  ;; Add aliases here for namespaces that you've included above. This adds an
  ;; `:as` form to a namespace: `(:require [clerk-utils.custom :as custom])`
  :aliases    {'custom 'clerk-utils.custom}})

;; ## JavaScript Libraries
;;
;;  `mentat.clerk-utils.sci` namespace's `register-js!` function allows you to
;;  make JavaScript libraries available to Clerk. The 2-arity version:

#_
(mentat.clerk-utils.sci/register-js! "react" react)

;; Would allow you to require the library in some notebook like so:

#_
(nextjournal.clerk/eval-cljs
 '(require '["react" :as my-alias]))

;; Alternatively, provide a global alias directly with the 3-arity version:
(mentat.clerk-utils.sci/register-js! "react" react 'react)
