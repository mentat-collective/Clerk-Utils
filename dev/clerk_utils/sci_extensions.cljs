(ns clerk-utils.sci-extensions
  (:require [clerk-utils.custom]
            [mentat.clerk-utils.sci]
            ["react" :as react]
            [sci.ctx-store]
            [sci.core :as sci]))

;; This form creates a "lives-within-SCI" version of the `clerk-utils.custom`
;; namespace by copying all public vars.
(def custom-namespace
  (sci/copy-ns clerk-utils.custom
               (sci/create-ns 'clerk-utils.custom)))

;; The `mentat.clerk-utils.sci` namespace has a function `js->sci-ns` that can
;; take a JavaScript namespace alias and generate a namespace object for sci:
(def react-namespace
  (mentat.clerk-utils.sci/js->sci-ns react))

;; This next form mutates SCI's default environment, merging in your custom code
;; on top of what Clerk has already configured.
(sci.ctx-store/swap-ctx!
 sci/merge-opts
 {;; Use `:classes` to expose JavaScript classes that you'd like to use in your
  ;; viewer code. `Math/sin` etc will work with this entry:
  :classes    {'Math js/Math}

  ;; Adding an entry to this map is equivalent to adding an entry like
  ;; `(:require [clerk-utils.custom])` to a Clojure namespace.
  :namespaces {'clerk-utils.custom custom-namespace
               'react react-namespace}

  ;; Add aliases here for namespaces that you've included above. This adds an
  ;; `:as` form to a namespace: `(:require [clerk-utils.custom :as custom])`
  :aliases    {'custom 'clerk-utils.custom}})
