^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.notebook
  (:require [mentat.clerk-utils :as u]
            [mentat.clerk-utils.docs :as docs]
            [mentat.clerk-utils.show :refer [show-sci]]
            [mentat.clerk-utils.viewers]
            [nextjournal.clerk :as clerk]))

;; # Clerk-Utils
;;
;; A small collection of functions and macros that have come in handy while
;; documenting libraries with Nextjournal's [Clerk](https://clerk.vision/).
;;
;; [![Build Status](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/clerk-utils/actions/workflows/kondo.yml)
;; [![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE)
;; [![cljdoc badge](https://cljdoc.org/badge/org.mentat/clerk-utils)](https://cljdoc.org/d/org.mentat/clerk-utils/CURRENT)
;; [![Clojars Project](https://img.shields.io/clojars/v/org.mentat/clerk-utils.svg)](https://clojars.org/org.mentat/clerk-utils)
;;
;; > The interactive documentation on this page was generated
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

^{:nextjournal.clerk/visibility {:code :hide}}
(docs/git-dependency
 "mentat-collective/clerk-utils")

;; `clerk-utils` does not currently export a Clerk dependency, so you'll have to
;; bring your own. For example:

^{:nextjournal.clerk/visibility {:code :hide}}
(docs/git-dependency
 "nextjournal/clerk"
 "4180ed31c2864687a770f6d4f625303bd8e75437")

;; Require `mentat.clerk-utils` in your namespace:

;; ```clj
;; (ns my-app
;;   (:require [mentat.clerk-utils :as u]
;;             [nextjournal.clerk :as-alias clerk]))
;; ```

;; > Note that `nextjournal.clerk` is included here using `:as-alias`. We're
;; > doing this to demo the [visibility macros](#visibility-macros) below, which
;; > allow you to write Clerk code in a library that will compile even if Clerk
;; > is not present as a dependency.

;; ## Custom ClojureScript Builds
;;
;; Clerk's [viewers](https://snapshots.nextjournal.com/clerk/build/b12d8b369a69ca4b41fcf4988194dfbc201c6e1c/book.html)
;; are written in ClojureScript code exposed
;; through [SCI](https://github.com/babashka/sci), the Small Clojure
;; Interpreter.
;;
;; If you want to use functions in your Clerk viewers that aren't included by
;; default, you'll need to:
;;
;; 1. Find some ClojureScript library you'd like to use, or write your own code
;;    in a `.cljs` file
;; 2. Add the new code to Clerk's [SCI
;;    environment](https://github.com/babashka/sci)
;; 3. Build a custom version of Clerk's ClojureScript that includes this new
;;    code.

;; The `mentat.clerk-utils.build` namespace contains versions of
;; `nextjournal.clerk/serve!`, `nextjournal.clerk/halt!` and
;; `nextjournal.clerk/build!` that handle this final step for you. They work
;; just like the originals, but optionally take a `:cljs-namespaces` key.

;; > For another way to expose custom ClojureScript in your notebooks, see
;; > the [`show-cljs` macro](#show-cljs-macro) section below.

;; For these functions to work you'll need to include the `clerk.render` git
;; dependency alongside your Clerk dependency:

;; ```clj
;; {io.github.nextjournal/clerk
;;  {:git/sha "4180ed31c2864687a770f6d4f625303bd8e75437"}
;;  io.github.nextjournal/clerk.render
;;  {:git/url "https://github.com/nextjournal/clerk"
;;   :git/sha "4180ed31c2864687a770f6d4f625303bd8e75437"
;;   :deps/root "render"}}
;; ```

;; > Make sure the hashes match!
;;
;; The next sections will show how to add custom ClojureScript, using this
;; project as an example.

;; #### Writing Custom ClojureScript
;;
;;  The [`clerk-utils.custom`](https://github.com/mentat-collective/clerk-utils/blob/main/dev/clerk_utils/custom.cljs)
;;  namespace looks like this:

;; ```clj
;; (ns clerk-utils.custom)

;; (defn square
;;   "Returns the square of `x`"
;;   [x]
;;   (* x x))
;; ```
;;
;; Our goal is to be able to use `clerk-utils.custom/square` in a custom viewer,
;; like this:

(def squared-viewer
  {:transform-fn clerk/mark-presented
   :render-fn
   '(fn [x]
      [:pre x "² is equal to " (clerk-utils.custom/square x) "."])})

;; The code works:

(clerk/with-viewer squared-viewer
  12)

;; ### Extending SCI

;; To make this work in your project, create a `cljs` file that looks like the
;; following. The namespace name doesn't matter, we'll use
;; `clerk-utils.sci-extensions` for this example.
;;
;; > Note the inline comments describing what each form does. The real file
;; lives [here](https://github.com/mentat-collective/clerk-utils/blob/main/dev/clerk_utils/sci_extensions.cljs).

;; ```clj
;; (ns clerk-utils.sci-extensions
;;   (:require [clerk-utils.custom]
;;             ["react" :as react]
;;             [sci.ctx-store]
;;             [sci.core :as sci]))

;; ;; ## Custom ClojureScript

;; ;; This form creates a "lives-within-SCI" version of the `clerk-utils.custom`
;; ;; namespace by copying all public vars.
;; (def custom-namespace
;;   (sci/copy-ns clerk-utils.custom
;;                (sci/create-ns 'clerk-utils.custom)))

;; ;; This next form mutates SCI's default environment, merging in your custom code
;; ;; on top of what Clerk has already configured.
;; (sci.ctx-store/swap-ctx!
;;  sci/merge-opts
;;  {;; Use `:classes` to expose JavaScript classes that you'd like to use in your
;;   ;; viewer code. `Math/sin` etc will work with this entry:
;;   :classes {'Math js/Math}
;;
;;   ;; `:js-libs` allows you to make libraries from NPM like "react" available to
;;   ;; `require` calls like this:
;;   ;;
;;   ;; ```clj
;;   ;; (nextjournal.clerk/eval-cljs
;;   ;;   '(require '["react" :as my-alias]))
;;   ;; ```
;;   :js-libs {"react" react}

;;   ;; Adding an entry to this map is equivalent to adding an entry like
;;   ;; `(:require [clerk-utils.custom])` to a Clojure namespace.
;;   :namespaces {'clerk-utils.custom custom-namespace})
;; ```
;;
;; The final step is to build a custom ClojureScript bundle that includes this
;; SCI-extending namespace. See [Interactive
;; Development](#interactive-development) for instructions on doing this during
;; live development, and [Static Build](#static-build) for instructions on doing
;; this for a static site export.
;;
;; Once you follow these steps, the [`show-sci` macro](#show-sci-macro) confirms
;; that the new code is available:

(clerk/eval-cljs
 '(require '["react" :as react]))

(show-sci
 [:div
  [:h4 "JavaScript example:"]
  [:pre "The react version is " react/version "."]
  [:h4 "ClojureScript example:"]
  [:pre "square works too: " (clerk-utils.custom/square 10)]])

;; ### Clerk Plugins

;; The following projects are all Clerk-compatible and contain namespaces that
;; make it easy to add them into Clerk's SCI environment with a single command.
;; See the pages for more detail:

;; - [Leva.cljs](https://leva.mentat.org)
;; - [Mafs.cljs](https://mafs.mentat.org)
;; - [JSXGraph.cljs](https://jsxgraph.mentat.org)
;; - [MathBox.cljs](https://mathbox.mentat.org)
;; - [MathLive.cljs](https://mathlive.mentat.org)

;; > For a full list of plugins, visit the ["Plugins"
;; > section](https://github.com/mentat-collective/awesome-clerk#plugins) of
;; > the [Awesome-Clerk](https://github.com/mentat-collective/awesome-clerk)
;; > page.

;; ### Interactive Development

;; The following command starts Clerk and
;; uses [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html) to
;; compile a ClojureScript file containing namespace
;; `clerk-utils.sci-extensions`:

;; ```clj
;; (require '[mentat.clerk-utils.build :as build])
;;
;; (build/serve!
;;  {:show? true
;;   :index "src/notebook.clj"
;;   :cljs-namespaces ['clerk-utils.sci-extensions]})
;; ```

;; [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html) serves
;; the compiled JavaScript from a server that watches all files on the classpath
;; and recompiles if anything changes.
;;
;; > This command will either generate a `package.json` file for you, or add
;; > dependencies to your existing `package.json`. These dependencies come from
;; > Clerk, and from any ClojureScript libraries that depend on code
;; > from [`npm`](https://www.npmjs.com/). Commit this file to CI, along with
;; > `package-lock.json`.
;;
;; When you're finished, call `(build/halt!)` to shut down Clerk and all
;; `shadow-cljs` processes.
;;
;; ### Static Build

;; `build/build!` with the `:cljs-namespaces` key will trigger a release build
;; of your ClojureScript:

;; ```clj
;; (build/build!
;;  {:index "src/notebook.clj"
;;   :cname "mysite.com"
;;   :cljs-namespaces ['clerk-utils.sci-extensions]})
;; ```
;;
;; By default, this command will produce a build in your project's
;; `public/build` directory. You can control this by passing your preferred
;; location with the `:output-path` key.
;;
;; If you intend to serve your static build on GitHub pages from a custom site
;; like `mysite.com`, passing the address via the `:cname` option will instruct
;; `build/build!` to create the required CNAME file in your `:output-path`.
;;
;; The build uses relative links for the custom JavaScript bundle; because of
;; this you won't be able to open `public/build/index.html` directly. You'll
;; need to run an HTTP server pointed at the `public/build` directory.
;;
;; One way to do this is with
;; Babashka's [http-server](https://github.com/babashka/http-server/#babashka).
;; The `bb serve` command declared in [this project's `bb.edn`
;; file](https://github.com/mentat-collective/clerk-utils/blob/main/bb.edn)
;; provides a nice template.
;;
;; For a probably-built-in solution, you could also use `python3`:

;; ```sh
;; # sub in your own port and output-dir
;; python3 -m http.server 1337 --directory public/build
;; ```
;;
;; #### Clerk Garden
;;
;; Nextjournal runs a site called [Clerk Garden](https://github.clerk.garden/)
;; that can generate and host your static builds for you. To make your project
;; Garden-compatible, you'll need to:

;; - Host the project on GitHub

;; - Create a `:nextjournal/clerk` alias in your `deps.edn` file with an
;;   `:exec-fn` pointing to `mentat.clerk-utils.build/build!`, and an
;;   `:exec-args` entry with all arguments you'd like to supply.
;;
;; - Visit https://clerk.garden/your-org/your-project to see your build.
;;
;; > Note that this URL will not automatically update on pushes to GitHub!
;; > You'll need to visit the url with `?update=1` appended to force it to
;; > update.
;;
;; Here is an example Garden-compatible `:aliases` entry from this project:

;; ```clj
;; :aliases
;; {:nextjournal/clerk
;;  {:extra-paths ["dev"]
;;   :extra-deps
;;   {io.github.nextjournal/clerk
;;    {:git/sha "4180ed31c2864687a770f6d4f625303bd8e75437"}
;;    io.github.nextjournal/clerk.render
;;    {:git/url "https://github.com/nextjournal/clerk"
;;     :git/sha "4180ed31c2864687a770f6d4f625303bd8e75437"
;;     :deps/root "render"}}
;;   :exec-fn mentat.clerk-utils.build/build!
;;   :exec-args
;;   {:index "dev/clerk_utils/notebook.clj"
;;    :paths ["dev/clerk_utils/show.cljc"]
;;    :cname "clerk-utils.mentat.org"
;;    :git/url "https://github.com/mentat-collective/clerk-utils"
;;    :cljs-namespaces ['clerk-utils.sci-extensions
;;                      'clerk-utils.show]}}}
;; ```
;;
;; I personally like to call `build!` from the REPL, so for this project I've
;; created a function `build!` in `dev/user.clj`:

;; ```clj
;; (def static-defaults
;;   {:index "dev/clerk_utils/notebook.clj"
;;    :paths ["dev/clerk_utils/show.cljc"]
;;    :cname "clerk-utils.mentat.org"
;;    :git/url "https://github.com/mentat-collective/clerk-utils"
;;    :cljs-namespaces ['clerk-utils.sci-extensions
;;                      'clerk-utils.show]})

;; (defn build!
;;   "Alias of [[mentat.clerk-utils.build/build!]] with [[static-defaults]] supplied as
;;   default arguments.

;;   Any supplied `opts` overrides the defaults."
;;   [opts]
;;   (mentat.clerk-utils.build/build!
;;    (merge static-defaults opts)))
;; ```
;;
;; and my actual `:nextjournal/clerk` alias contains a `:exec-fn user/build!`
;; entry.
;;
;; ## Project Template
;;
;; `clerk-utils` includes
;; a [`deps-new`](https://github.com/seancorfield/deps-new) template called
;; [`clerk-utils/custom`](https://github.com/mentat-collective/clerk-utils/tree/main/resources/clerk_utils/custom)
;; that makes it easy to configure a new Clerk project with everything described
;; in [Custom ClojureScript Builds](#custom-clojurescript-builds) already
;; configured.

;; First, install the [`deps-new`](https://github.com/seancorfield/deps-new) tool:

;; ```sh
;; clojure -Ttools install io.github.seancorfield/deps-new '{:git/tag "v0.4.13"}' :as new
;; ```

;; To create a new Clerk project based on
;; [`clerk-utils/custom`](https://github.com/mentat-collective/clerk-utils/tree/main/resources/clerk_utils/custom)
;; in a folder called `my-notebook-project`, run the following command:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md
 (format "
```sh
clojure -Sdeps '{:deps {io.github.mentat-collective/clerk-utils {:git/sha \"%s\"}}}' \\
-Tnew create \\
:template clerk-utils/custom \\
:name myusername/my-notebook-project
```" (docs/git-sha)))

;; The `README.md` file in the generated project contains information on how to
;; extend and develop within the new project.

;; If you have an existing Clerk notebook project and are considering adding
;; support for custom ClojureScript, you might consider
;; using [`clerk-utils/custom`](https://github.com/mentat-collective/clerk-utils/tree/main/resources/clerk_utils/custom)
;; to get some ideas on how to structure your own project.

;; ## Visibility Macros

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

;; Other data structures are presented with `[nextjournal.clerk.viewer/inspect ...]`:

(show-sci
 {:key "value"})

;; To present a vector as code, manually wrap it in `[v/inspect ...]`:

(show-sci
 [nextjournal.clerk.viewer/inspect
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
  [nextjournal.clerk.viewer/inspect
   (nextjournal.clerk.viewer/tex
    (str @!state
         "^2 = "
         (square @!state)))]])

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

;; ## `q` Macro for Viewers
;;
;; The latest versions of Clerk require you to fully resolve the namespace of
;; any function you use inside the `:render-fn` of [your custom Clerk
;; viewer](https://book.clerk.vision/#writing-viewers).
;;
;; The `mentat.clerk-utils.viewers/q` macro is similar to `clojure.core/quote`,
;; but
;;
;; - expands any symbol namespaces that are present as aliases
;; - allows for splicing and unquote splicing
;;
;; For example, if we create an alias for `nextjournal.clerk.viewer`:

(require '[nextjournal.clerk.viewer :as-alias v])

;; then `q` will expand any `v/<symbol>` calls, and leave other prefixes (or
;; non-namespaced symbols) alone:

(mentat.clerk-utils.viewers/q
 (fn [x]
   [v/inspect #{prefix/x y}]))

;; `q` also allows you to splice in values from the `clj` environment:

(let [defaults {:key "value"}
      entries  [1 2 3]]
  (mentat.clerk-utils.viewers/q
   (fn [x]
     [myns/SomeComponent
      ~defaults
      [~@entries x]])))

;; These features allow you to build up viewers in pieces, like this:

(def inner-call
  (mentat.clerk-utils.viewers/q
   [myns/InnerComponent
    {:k "v"}
    "inner call"]))

(def render-fn
  (mentat.clerk-utils.viewers/q
   (fn [x]
     [myns/OuterComponent
      {:other "props"}
      ~inner-call])))

;; ## CSS Functions

;; `mentat.clerk-utils.css` includes functions that allow you to inject CSS
;; files into Clerk's header. https://mafs.mentat.org uses `set-css!` in the
;; project's `user.clj` file like so:

;; ```clj
;; (mentat.clerk-utils.css/set-css!
;;  "https://unpkg.com/computer-modern@0.1.2/cmu-serif.css"
;;  "https://unpkg.com/mafs@0.11.4/core.css"
;;  "https://unpkg.com/mafs@0.11.4/font.css")
;; ```

;; `add-css!` is similar but appends entries, and `reset-css!` clears out this
;; accumulating list.

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

;; The following documentation notebooks include examples of
;; the [`show-sci`](#show-sci-macro) macro:

;; - [JSXGraph.cljs](https://jsxgraph.mentat.org)
;; - [MathLive.cljs](https://mathlive.mentat.org)
;; - [MathBox.cljs](https://mathbox.mentat.org)
;; - [Mafs.cljs](https://mafs.mentat.org)
;; - [Leva.cljs](https://leva.mentat.org)
;;
;; Many of the [MathBox.org
;; examples](https://mathbox.mentat.org/dev/mathbox/examples/index.html) use
;; [`show-cljs`](#show-cljs-macro).

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

;; Copyright © 2022-2023 Sam Ritchie.

;; Distributed under the [MIT
;; License](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE).
;; See [LICENSE](https://github.com/mentat-collective/clerk-utils/blob/main/LICENSE).
