^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.show
  (:require [mentat.clerk-utils.show :refer [show-cljs show-sci]]))

;; # show-cljs and CLJC Notebooks
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
  (str "I was compiled by ClojureScript version "
       *clojurescript-version*
       ".")])

;; `show-cljs` lives in `mentat.clerk-utils.show`:
;;
;; ```clj
;; (ns my-app
;;   (:require [mentat.clerk-utils.show :refer [show-cljs show-sci]]
;;             [nextjournal.clerk :as-alias clerk]))
;; ```
;;
;; > This macro originally comes from Matt
;; > Huebert's [clerk-cljs](https://github.com/mhuebert/clerk-cljs) library, and
;; > is used throughout https://inside-out.matt.is/.
;;
;; ## Project Requirements
;;
;; `show-cljs` required a custom ClojureScript build. Follow the [instructions
;; on the main documentation page](index.html#custom-clojurescript-builds) to
;; set this up, and make sure that you either
;;
;; - include your `cljc` files in the `:cljs-namespaces` sequence, or
;; - require your `cljc` namespaces inside of some _other_ `cljs` file in the
;;   `:cljs-namespaces` sequence.
;;
;; As long as each file is required somewhere it will be transitively pulled
;; into the build.

;; ## `show-cljs` basics

;; As with [`show-sci`](index.html#show-sci-macro) vectors are interpreted as
;; Reagent components:

(show-cljs
 (let [text (str "Hi from ClojureScript version "
                 *clojurescript-version*
                 "!")]
   [:pre text]))

;; Other data structures are presented with `[v/inspect ...]`:

(show-cljs
 {:key "value"})

;; To present a vector as code, tag it with `^:inspect` to set the `:inspect`
;; entry in the vector's metadata to `true`:

(show-cljs
 (let [text "Include any Reagent vector!"]
   ^:inspect
   [:pre text]))

;; ## Automatic Reloading
;;
;; If you have a `shadow-cljs` watcher process running, any change to the code
;; within a `show-cljs` block is automatically recompiled and reloaded in the
;; Clerk notebook. You might see `loading...` appear if Clerk's load happens
;; before `shadow-cljs` finishes compilation.

;; ## Exporting code from ClojureScript
;;
;; As with `show-sci`, multiple forms are allowed. All are evaluated and only
;; the final form is presented:

(show-cljs
 (defn quizzical [s]
   (str s "...?"))

 [:pre (quizzical "Hi")])

;; Any `defn` you include will be available to forms below:

(show-cljs
 [:pre (quizzical "Still here")])

;; By default, these `defn`s will _not_ be available to the SCI environment that
;; run Clerk's viewers:

(show-sci
 [:pre (quizzical "Still here")])

;; To make a `defn` available to the SCI environment, use `^:export` metadata on
;; the `defn`. You'll need to reload the page; live reload does not work in this
;; case.

;; > Note that this only works with a top-level `defn` inside of `show-cljs`! It
;; > will fail if the `defn` lives inside of a `let` binding, `do`, or anything
;; > similar.

(show-cljs
 (defn ^:export winking [s]
   (str s " ;)")))

;; On the SCI side, you'll need to call the function via JavaScript. Note
;;
;; - the `js/` prefix
;; - any dashes in the namespace have to be converted to underscores
;; - no slash between namespace and function name

(show-sci
 [:pre (js/clerk_utils.show.winking "Yo")])

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
