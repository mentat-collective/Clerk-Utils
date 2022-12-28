^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.show
  (:require [mentat.clerk-utils.show :refer [show-cljs]]))

;; # `show-cljs` and CLJC Notebooks
;;
;; This notebook documents the `show-cljs` macro. Use this macro if you want
;; compile ClojureScript code for use in Clerk's browser page.
;;
;; Some reasons you might do this:
;;
;; - You need more performance than the
;;   standard [SCI](https://github.com/babashka/sci) environment can offer
;; - Your code uses macros that aren't supported via SCI
;; - You want the code that you're writing and documenting to be available to
;;   other ClojureScript consumers as a library

;; `show-cljs` allows you to write ClojureScript code that passes directly
;; through the ClojureScript compiler, but still renders and runs in Clerk:

(show-cljs
 [:pre
  (str "I'm running in ClojureScript version "
       *clojurescript-version*
       ".")])

;; `show-cljs` lives in `mentat.clerk-utils.show`:
;;
;; ```clj
;; (ns my-app
;;   (:require [mentat.clerk-utils.show :refer [show-cljs]]
;;             [nextjournal.clerk :as-alias clerk]))
;; ```
;;
;; > TODO note that this originally came from Matt
;; > Huebert's [clerk-cljs](https://github.com/mhuebert/clerk-cljs).
;;
;; ## Project Setup
;;
;; To compile cljc files and render them in Clerk you'll need to configure your
;; project to watch and build your cljc files and make them available to Clerk.
;;
;; This section will discuss how to:
;;
;; - Configure `deps.edn` with the required entries for a custom Clerk
;;  ClojureScript build
;; - package.json and node
;; - Set up shadow-cljs
;; - Generate a static build of your Clerk project that can make use of your
;;   custom ClojureScript bundle and reach readers via Github Pages or [Clerk's
;;   Garden](https://github.clerk.garden/) static publishing system.

;;
;; ### deps.edn
;;
;; TODO note the Clerk Render dependency, making sure that the shadow-cljs dep
;; matches the version in package.json, etc.

;; ### shadow-cljs
;;
;; TODO what entries, how it ties to deps.edn, note the build output and the
;; NAME of the build.
;;
;; ### Interactive Clerk with Custom ClojureScript
;;
;; Show the simpler use case where we include the cljc file in shadow-cljs.edn,
;; and the more complex case where you have straight cljs files, cljc etc all
;; required from a single namespace that coordinates everything.
;;
;; (This won't include any SCI customization, but I can crib from this.)
;;
;; ### user.clj

;; ### Static Build
;;
;; How to get this working with Garden or Github Pages. Link to this project's
;; Github Actions, also note the `bb.edn` method for making this all a little
;; more smooth, and `npm run` scripts.


;; ## `show-cljs`
;;
;; - last form is treated as Reagent, or a vector if marked with inspect metadata
;;
;; - defns work
;;
;; - any defn form at the top level is pulled UP ahead of other stuff. This is so that `^:export` works.
;;
;; ## `show-cljs` and `show-sci` together
;;
;; - show how to export from cljs and recover it inside of sci

;; ## Styling
;;
;; Note that we include two CSS tags so you can style the output if you want...

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
