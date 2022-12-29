^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.notebook
  (:require [mentat.clerk-utils :as u]
            [mentat.clerk-utils.show :refer [show-sci]]
            [nextjournal.clerk :as-alias clerk]))

;; # clerk-utils
;;
;; A small collection of functions and macros that have come in handy while
;; documenting libraries with Nextjournal's [Clerk](https://clerk.vision/).
;;
;; [![Build Status](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml)
;; [![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE)
;; [![cljdoc badge](https://cljdoc.org/badge/org.mentat/clerk-utils)](https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT)
;; [![Clojars Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)
;;
;; > The interactive documentation on this page was generated from [this source
;; > file](https://github.com/mentat-collective/clerk-utils/blob/$GIT_SHA/dev/clerk_utils/notebook.clj)
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

;; ## Quickstart
;;
;; Install `clerk-utils` into your Clojure project using the instructions at its
;; Clojars page:

;; [![Clojars
;; Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)
;;
;; Or grab the most recent code using a Git dependency:
;;
;; ```clj
;; ;; deps
;; {io.github.mentat-collective/clerk-utils
;;   {:git/sha "$GIT_SHA"}}
;; ```

;; Require `mentat.clerk-utils` in your namespace:

;; ```clj
;; (ns my-app
;;   (:require [mentat.clerk-utils :as u]
;;             [nextjournal.clerk :as-alias clerk]))
;; ```

;; ## Visibility Macros
;;
;; These macros allow you to include Clerk code in a library or project that may
;; not have `nextjournal.clerk` available on the classpath.
;;
;;
;; ### ->clerk
;;
;; Wrapping a form in `->clerk` will cause the form to treated as a `comment` if
;; `nextjournal.clerk` is not present on the classpath.

(u/->clerk
 (clerk/tex "1+x^2"))

;; You might use this when documenting a piece of production code that you want
;; to develop using Clerk, while excluding the Clerk dependency from the
;; production artifact.
;;
;; If you're using Clojure 1.11 or above, use the `:as-alias` form of `require`
;; to get nice prefixes for your Clerk functions that will still work if Clerk
;; is not available;

;; ```clj
;; (require '[nextjournal.clerk :as-alias clerk])
;; ```
;;
;; ### ->clerk-only
;;
;; Similar, but only evaluates its contents for Clerk. These forms will not be
;; accessible from the REPL, and won't run in the REPL process (or when other
;; namespaces require the namespace containing the form.)

(u/->clerk-only
 ;; some expensive visualization...
 [1 2 3])

;; ## `show-sci` macro

;; `show-sci` lets you inject Reagent directly into Clerk's browser page. You
;; might want to do this when crafting some UI-only code in Clerk.
;;
;; This function lives in `mentat.clerk-utils.show`:

;; ```clj
;; (require '[mentat.clerk-utils.show :refer [show-sci]])
;; ```
;;
;; The name `show-sci` refers to the fact that Clerk's viewers are evaluated by
;; SCI, the [Small Clojure Interpreter](https://github.com/borkdude/sci).
;;
;; > To compile forms using the full Clojurescript compiler,
;; > see [`show-cljs`](#show-cljs-macro) below.
;;
;; Vectors are interpreted as Reagent components:

(show-sci
 (let [text "Include any Reagent vector!"]
   [:pre text]))

;; Other data structures are presented with `[v/inspect ...]`:

(show-sci
 {:key "value"})

;; To present a vector as code, manually wrap it in `[v/inspect ...]`:

(show-sci
 [v/inspect
  (let [text "Include any Reagent vector!"]
    [:pre text])])

;; Multiple forms are allowed. All are evaluated and only the final form is
;; presented:

(show-sci
 (defn exclaim [s]
   (str s "!"))

 [:pre (exclaim "Hi")])

;; Any `defn` you include will be available to forms below:

(show-sci
 [:pre (exclaim "Still here")])

;; ### Client / Server Example
;;
;; Annotate a `var` definition bound to an atom with `^{::clerk/sync true}` to
;; synchronize it between client and server.

^{::clerk/sync true}
(defonce !state
  (atom 0))

;; Now any updates to this atom on the client will show up in client code:

(show-sci
 (defn square [x]
   (* x x))

 ;; Note that you must prepend the namespace onto the var annotated with
 ;; `::clerk/sync`:
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

;; These client-side changes will propagate to the server-side version of the
;; atom, and will be available at the REPL:

(clerk/md
 (str "The server-side value of $x="
      @!state
      "$ changes too."))

;; ## `show-cljs` Macro

;; `show-cljs` is similar to `show-sci`, but allows you to compile its forms using the
;; full Clojurescript compiler.

;; `show-cljs` lives in `mentat.clerk-utils.show`:
;;
;; ```clj
;; (require '[mentat.clerk-utils.show :refer [show-cljs]])
;; ```
;;
;; This macro only works inside of a [`cljc`
;; file](https://clojure.org/guides/reader_conditionals), which both the Clojure
;; and ClojureScript compilers are able to read. This more complex use case is
;; documented in the [`show-cljs` documentation
;; notebook](dev/clerk_utils/show.html).

;; ## clj-kondo config

;; `clerk-utils` ships with a configuration that allows
;; [clj-kondo](https://github.com/clj-kondo/clj-kondo) to lint the library's
;; macros.

;; To install the exported linter configuration:

;; 1. Install clj-kondo using [these
;;    instructions](https://github.com/clj-kondo/clj-kondo/blob/master/doc/install.md).
;;    I highly recommend configuring [editor
;;    integration](https://github.com/clj-kondo/clj-kondo/blob/master/doc/editor-integration.md)
;;    for your text editor.

;; 2. If it doesn't exist yet, create a `.clj-kondo` folder in your project:

;; ```sh
;; mkdir .clj-kondo
;; ```

;; 3. Run `clj-kondo` using the following command. This will import the
;; `clerk-utils` config and populate `clj-kondo`'s cache with linting
;; information about all of your dependencies:
;;
;; ```sh
;; # If you're using Leiningen:
;; clj-kondo --copy-configs --dependencies --lint$(lein classpath)"

;; # If you're using deps.edn:
;; clj-kondo --copy-configs --dependencies --lint "$(clojure -Spath)"
;; ```
;;
;; > The steps listed here mirror the [instructions in the clj-kondo
;; repo](https://github.com/clj-kondo/clj-kondo/blob/master/doc/config.md#importing).

;; ## Who is using clerk-utils?

;; The following documentation notebooks include examples of the `cljs` macro:

;; - [JSXGraph.cljs](https://jsxgraph.mentat.org)
;; - [MathLive.cljs](https://mathlive.mentat.org)
;; - [MathBox.cljs](https://mathbox.mentat.org)

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
