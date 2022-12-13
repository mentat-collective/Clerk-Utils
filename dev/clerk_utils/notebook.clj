^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.notebook
  (:require [mentat.clerk-utils :refer [cljs ->clerk]]
            [nextjournal.clerk :as-alias clerk]))

;; # clerk-utils
;;
;; TODO blurb.

;; [![Build Status](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml)
;; [![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE)
;; [![cljdoc badge](https://cljdoc.org/badge/org.mentat/clerk-utils)](https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT)
;; [![Clojars Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)
;;
;; > The interactive documentation on this page was generated from [this source
;; > file](https://github.com/mentat-collective/clerk-utils/blob/$GIT_SHA/dev/jsxgraph/notebook.clj)
;; > using [Clerk](https://github.com/nextjournal/clerk). Follow
;; > the [instructions in the
;; > README](https://github.com/mentat-collective/clerk-utils/tree/main#interactive-documentation-via-clerk)
;; > to run and modify this notebook on your machine!
;; >
;; > See the [Github
;; > project](https://github.com/mentat-collective/clerk-utils) for more
;; > details, and the [cljdoc
;; > page](https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT/doc/readme) for
;; > detailed API documentation.

;; ## ->clerk
;;
;; This will elide anything that is NOT

(->clerk
 (clerk/tex "1+x"))

;;
;; ## `cljs` macro
;;
;; This will let you inject Reagent directly. You might want to do this when
;; crafting some UI-only code in Clerk.

(cljs
 [:pre "Include any Reagent vector."])

;; ### Other data structures

(cljs
 {:key "value"})

(cljs
 #{1 2 3})

;; ### Client / Server Example

^{:nextjournal.clerk/sync true}
(defonce !state
  (atom 0))

(cljs
 (defn square [x]
   (* x x))

 (let [!state clerk-utils.notebook/!state]
   [:<>
    [:div
     [:input
      {:type :range :min 0 :max 10 :step 1
       :value @!state
       :on-change
       (fn [target]
         (let [v (.. target -target -value)]
           (reset! !state (js/parseInt v))))}]
     " " @!state]
    [v/inspect
     (v/tex
      (str @!state
           "^2 = "
           (square @!state)))]]))

(clerk/md
 (str "The server-side value of $x="
      @!state
      "$ changes too."))

;; multiple forms
;;
;; other data structures
;;
;; call inspect etc
;;
;; TODO publish the clj-kondo config too, note how to use it
;;
;; TODO note the `->clerk`.

;; ## Thanks and Support

;; To support this work and my other open source projects, consider sponsoring
;; me via my [GitHub Sponsors page](https://github.com/sponsors/sritchie). Thank
;; you to my current sponsors!

;; I'm grateful to [Clojurists Together](https://www.clojuriststogether.org/)
;; for financial support during this library's creation. Please
;; consider [becoming a member](https://www.clojuriststogether.org/developers/)
;; to support this work and projects like it.
;;
;; For more information on me and my work, visit https://samritchie.io.

;; ## License

;; Copyright © 2022 Sam Ritchie.

;; Distributed under the [MIT
;; License](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE).
;; See [LICENSE](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE).
