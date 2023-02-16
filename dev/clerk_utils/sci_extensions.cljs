(ns clerk-utils.sci-extensions
  (:require [clerk-utils.custom]
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
  :classes {'Math js/Math}

  ;; `:js-libs` allows you to make libraries from NPM like "react" available to
  ;; `require` calls like this:
  ;;
  ;; ```clj
  ;; (nextjournal.clerk/eval-cljs
  ;;   '(require '["react" :as my-alias]))
  ;; ```
  :js-libs {"react" react}

  ;; Adding an entry to this map is equivalent to adding an entry like
  ;; `(:require [clerk-utils.custom])` to a Clojure namespace.
  :namespaces {'clerk-utils.custom custom-namespace}})
