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
;; Running Clerk interactively with cljc files a custom ClojureScript build
;; requires some additional project configuration. This is fairly involved!
;; Follow the [instructions below](#project-setup), and then create your first
;; `cljc` file and require `mentat.clerk-utils.show` as above.


;; ## `show-cljs` basics

;; As with `show-sci`, Vectors are interpreted as Reagent components:

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

;; ## Project Setup
;;
;; To compile cljc files and render them in Clerk you'll need to configure your
;; project to watch and build your cljc files and make them available to Clerk.
;;
;; This section will cover each of the following steps:
;;
;; - Configure `deps.edn` with the required entries for a custom Clerk
;;  ClojureScript build
;; - Set up [`shadow-cljs.edn`](https://shadow-cljs.github.io/docs/UsersGuide.html)
;; - Configure `package.json`
;; - Configure `user.clj`
;; - Coordinate tasks with [Babashka](https://github.com/babashka/babashka) via
;;   `bb.edn`
;; - Ship a static build via Github Pages or [Clerk's
;;   Garden](https://github.clerk.garden/) static publishing system.
;;
;; ### deps.edn
;;
;; To build custom ClojureScript you'll need to depend
;; on [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html)
;; and [`clerk-render`](https://github.com/nextjournal/clerk/tree/main/render).
;; I like to add `shadow-cljs` under a `:dev` alias, and `clerk-render` under a
;; new `:nextjournal/clerk` alias.
;;
;;
;; Here is an annotated example `:aliases` entry in `deps.edn`:
;;
;; ```clj
;; {:aliases
;;  {:dev
;;   {:extra-deps
;;    {org.clojure/clojure {:mvn/version "1.11.1"}
;;
;;     ;; This version needs to match the shadow-cljs version declared in
;;     ;; `package.json`.
;;     thheller/shadow-cljs {:mvn/version "2.20.14"}
;;
;;     ;; the clojurescript version must match (or exceed?) the version declared by
;;     ;; shadow-cljs:
;;     ;; https://github.com/thheller/shadow-cljs/blob/master/project.clj#L40
;;     org.clojure/clojurescript {:mvn/version "1.11.60"}}}
;;
;;   :nextjournal/clerk
;;   {;; I include :extra-paths here because my documentation notebooks live in
;;    ;; `"dev"` and I don't want to deploy them with the library. This isn't
;;    ;; necessary if your notebooks live alongside your source code.
;;    :extra-paths ["dev"]
;;    :extra-deps
;;    ;; You'll need a dependency on clerk and clerk.render. The latter lives
;;    ;; inside of the Clerk repository, so the versions here need to match.
;;    ;;
;;    ;; I recommend using git dependencies here. Use the same `:git/sha` for each
;;    ;; dependency and bump them together.
;;    {io.github.nextjournal/clerk
;;     {:git/sha "ec666210f11763fce4fec74072acca1e6525d29f"}
;;     io.github.nextjournal/clerk.render
;;     {:git/url "https://github.com/nextjournal/clerk"
;;      :git/sha "ec666210f11763fce4fec74072acca1e6525d29f"
;;      :deps/root "render"}}
;;
;;    ;; Note that this points to a function in your `user.clj`, explained below.
;;    :exec-fn user/garden!}}}
;; ```

;; ### shadow-cljs
;;
;; Next, create a file called `shadow-cljs.edn` in the root of your project.
;; This file configured
;; the [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html)
;; ClojureScript build tool to generate the JavaScript bundle that Clerk needs
;; to run in development and static-build modes.
;;
;; Install `shadow-cljs` via NPM ([guide
;; here](https://shadow-cljs.github.io/docs/UsersGuide.html#_standalone_via_code_npm_code)).
;; If you've installed `shadow-cljs` globally by running `npm install -g
;; shadow-cljs`, run the following command to start a process that builds the JS
;; and rebuilds it on any source code change:
;;
;; ```sh
;; shadow-cljs watch clerk
;; ```
;;
;; Build your final production bundle with
;;
;; ```sh
;; shadow-cljs watch clerk
;; ```
;;
;; Here's an annotated example `shadow-cljs.edn` file:
;;
;;```clj
;; {;; This first entry tells shadow-cljs to pull its dependencies from `deps.edn`,
;;  ;; and to merge in the specified aliases when doing so.
;;  :deps
;;  {:aliases [:dev :nextjournal/clerk]}
;;  ;; I set this port so that the server started by shadow-cljs in this project
;;  ;; doesn't clash with other projects I may have running. You'll need the port
;;  ;; in `user.clj` later.
;;  :dev-http {8765 "public"}
;;  :builds
;;
;;  ;; This `:clerk` keyword explains the `clerk` in `shadow-cljs watch clerk` above.
;;  {:clerk
;;   {:target :esm
;;    :runtime :browser
;;    :output-dir "public/js"
;;    :compiler-options
;;    {:infer-externs :auto
;;     :optimizations :advanced}
;;    :modules
;;    ;; Note the keyword `:main`. Your final JS bundle will live at
;;    ;; `<output-dir>/main.js`. Change this keyword or the `:output-dir` above to
;;    ;; tune this.
;;    {:main
;;     {:entries
;;      ;; shadow-cljs will include these namespaces and any namespaces they depend
;;      ;; on in your final bundle. The `static-app` entry is required.
;;      ;;
;;      ;; You'll need to list your `cljc` namespaces here as well for `show-cljs`
;;      ;; to work. You could also add a single namespace like
;;      ;; `my-project.extensions` and a corresponding
;;      ;; `dev/my_project/extensions.cljs`, and `require` all of your `cljc` and
;;      ;; `cljs` files inside that namespace.
;;      [nextjournal.clerk.static-app
;;       clerk-utils.show]}}
;;    :js-options
;;    {:output-feature-set :es8}}}}
;; ```
;;
;; ### package.json
;;
;; `shadow-cljs` defers to `deps.edn` for any ClojureScript dependencies, and a
;; file called `package.json` for any straight JavaScript dependencies. Create
;; `package.json` in your project's root, modeled after the template below.
;;
;; I also like to use `package.json` for its "scripts" feature; this lets me
;; call `shadow-cljs` as a local library instead of a globally installed
;; library, which helps make sure that its version matches the version declared
;; in `deps.edn`.
;;
;; `shadow-cljs` will automatically add any JavaScript dependencies declared by
;; its ClojureScript to `package.json`, and install them into the project. The
;; first time you build your JavaScript, `package.json` will get a bunch of new
;; entries. Commit these changes to version control.
;;
;; If _your_ library has JavaScript dependencies that you want consumers to
;; install, declare these in `src/deps.cljs` (like [`jsxgraph.cljs` does
;; here](https://github.com/mentat-collective/jsxgraph.cljs/blob/main/src/deps.cljs),
;; for example).
;;
;; The following annotated `package.json` includes `shadow-cljs` and a few
;; scripts I've found helpful. Delete them, or call them with `npm run
;; <script-name>`.
;;
;; > Comments are not allowed in JSON, so DELETE THE COMMENTS after copying this
;; > code to your `package.json`!
;;
;; ```json
;; {
;;   "devDependencies": {
;;     "shadow-cljs": "^2.20.14",
;;
;;     // This dep and the `gh-pages` script below allows you to ship a static
;;     // build to Github Pages.
;;     "gh-pages": "^3.2.3",
;;
;;     // This is helpful for viewing a static build locally.
;;     "http-server": "^14.1.1"
;;   },
;;   "scripts": {
;;     // Run the build-with-file-watcher process from the version of shadow-cljs
;;     // declared above.
;;     "watch-clerk": "shadow-cljs watch clerk",
;;
;;     // -o opens the browser, and -c-1 disables caching. This is important,
;;     // because the JS filename doesn't change when shadow-cljs rebuilds it!
;;     "serve": "http-server -c-1 -o",
;;
;;     // deploys the static build to Github Pages.
;;     "gh-pages": "gh-pages -d public --dotfiles true",
;;
;;     // Generates a production version of this project's JavaScript bundle.
;;     "release-clerk": "shadow-cljs release clerk"
;;   }
;; }
;; ```
;;
;; ### user.clj
;;
;; To run Clerk with a custom ClojureScript bundle you'll need to modify its
;; internal config on startup. I prefer to do this via a suite of functions in
;; `dev/user.clj`. The `user` namespace is automatically loaded by Clojure, so
;; these functions will be available at a fresh REPL.
;;
;; Here is a template for a `user.clj` file that can handle starting Clerk in
;; interactive mode, or building a static Clerk bundle to serve in Github Pages
;; or Clerk's Garden:
;;
;; ```clj
;; (ns user
;;   "This namespace template assumes that your project lives at
;;   https://github.com/$YOUR_ORG/$PROJECT_NAME. Replace all occurrences of
;;   $YOUR_ORG and $PROJECT_NAME below accordingly."
;;   (:require [babashka.fs :as fs]
;;             [clojure.java.shell :refer [sh]]
;;             [clojure.string]
;;             [nextjournal.clerk :as clerk]
;;             [nextjournal.clerk.config :as config]
;;             [nextjournal.clerk.view]
;;             [nextjournal.clerk.viewer :as cv]
;;             [shadow.cljs.devtools.api :as shadow]))

;; (def index
;;   ;; Replace this with the namespace you'd like to use as your index.
;;   "dev/clerk_utils/notebook.clj")

;; (def build-target
;;   ;; This is the argument to `clerk/build!`.
;;   {:index index
;;    :paths ["dev/clerk_utils/show.cljc"]})

;; (def ^{:doc "static site defaults for local and github-pages modes."}
;;   defaults
;;   {;; This matches the path in `shadow-cljs.edn`.
;;    :out-path   "public"
;;    ;; Depending on where you are serving Github Pages from, you might need to
;;    ;; modify this. This setting is good if you are using a custom top level
;;    ;; domain. If you are serving from `$YOUR_ORG.github.io/$PROJECT_NAME`, then
;;    ;; change this string to `/$PROJECT_NAME/` with the trailing backslash.
;;    :cas-prefix "/"})

;; (def ^{:doc "static site defaults for Clerk's Garden CDN."}
;;   garden-defaults
;;   {:cas-prefix "/$YOUR_ORG/$PROJECT_NAME/commit/$GIT_SHA/"})

;; (defn start!
;;   "Runs [[clerk/serve!]] with our custom JS. Run this after generating custom JS
;;   with shadow in either production or `watch` mode.

;;   Note that this port must match the port declared in `shadow-cljs.edn`."
;;   []
;;   (swap! config/!resource->url
;;          assoc
;;          "/js/viewer.js" "http://localhost:8765/js/main.js")
;;   (clerk/serve!
;;    {:browse? true
;;     :watch-paths ["dev"]})
;;   (Thread/sleep 500)
;;   (clerk/show! index))

;; (defn static-build!
;;   "This task is used to generate static sites for local use, github pages
;;   deployment and Clerk's Garden CDN.

;;   Accepts a map of options `opts` and runs the following tasks:

;;   - Slurps the output of the shadow-cljs `:clerk` build from `public/js/main.js`
;;     and pushes it into a CAS located at `(str (:out-path opts) \"/js/_data\")`.

;;   - Configures Clerk to generate files that load the js from the generated name
;;     above, stored in `(str (:cas-prefix opts) \"/js/_data\")`

;;   - Creates a static build of the single namespace [[index]] at `(str (:out-path
;;     opts) \"/index.html\")`

;;   All `opts` are forwarded to [[nextjournal.clerk/build!]]."
;;   [opts]
;;   (let [{:keys [out-path cas-prefix]} (merge defaults opts)
;;         cas (cv/store+get-cas-url!
;;              {:out-path (str out-path "/js") :ext "js"}
;;              (fs/read-all-bytes "public/js/main.js"))]
;;     (swap! config/!resource->url assoc
;;            "/js/viewer.js"
;;            (str cas-prefix "js/" cas))
;;     (clerk/build!
;;      (merge build-target
;;             (assoc opts :out-path out-path)))))

;; (defn garden!
;;   "Standalone executable function that runs [[static-build!]] configured for
;;   Clerk's Garden. See [[garden-defaults]] for customizations
;;   to [[static-build!]]."
;;   [opts]
;;   (println "Running npm install...")
;;   (println
;;    (sh "npm" "install"))
;;   (shadow/release! :clerk)
;;   (static-build!
;;    (merge garden-defaults opts)))
;; ```
;;
;; ### bb.edn
;;
;; The [Babashka](https://github.com/babashka/babashka) task runner does a great
;; job of coordinating calls to all of the build tools at play above.
;;
;; Add a file called `bb.edn` to your project's root with the following
;; contents. After installing Babashka, call each task with `bb <task-name>`.
;; `bb tasks` will generate a list of all tasks with their docstrings.

;; Read through the tasks and customize the template for your project. (This
;; template also includes a `bb lint` task!)
;;
;; ```clj
;; {:pods {clj-kondo/clj-kondo {:version "2022.12.10"}}
;;  :tasks
;;  {:requires ([pod.borkdude.clj-kondo :as clj-kondo])

;;   dev
;;   {:doc "Start a shadow-cljs watch process that generates this project's custom JS."
;;    :task
;;    (do (shell "npm install")
;;        (shell "npm run watch-clerk"))}

;;   start-clerk
;;   {:doc "Start a Clerk dev server configured with this project's custom JS."
;;    :task
;;    (shell "clojure -X:dev:nextjournal/clerk user/start!")}

;;   publish-gh-pages
;;   {:doc "Generate a fresh static build."
;;    :task
;;    (do (shell "npm ci")
;;        (shell "npm run release-clerk")
;;        (apply shell
;;               "clojure -X:dev:nextjournal/clerk user/static-build!"
;;               *command-line-args*)
;;        ;; If you want to serve your project from a custom domain hosted by
;;        ;; Github Pages, Remove the #_ and add your custom URL here.
;;        #_
;;        (spit "./public/CNAME" "YOUR_PROJECT_URL")

;;        ;; This is necessary for folders with underscores to work, like the one
;;        ;; that Clerk uses to store data for its CAS.
;;        (spit "./public/.nojekyll" ""))}

;;   release-gh-pages
;;   {:doc "Generate a fresh static build and release it to Github Pages."
;;    :task
;;    (do (shell "rm -rf public")
;;        (run 'publish-gh-pages)
;;        (shell "npm run gh-pages"))}

;;   publish-local
;;   {:doc "Generate a fresh static build in the `public` folder and start a local
;;   webserver."
;;    :task
;;    (do (run 'publish-gh-pages)
;;        (shell "npm run serve"))}

;;   lint
;;   {:doc "Lint the src and dev directories with clj-kondo."
;;    :task (clj-kondo/print!
;;           (clj-kondo/run! {:lint ["src" "dev"]}))}}}
;; ```
;;
;; Github Pages Action
;;
;; If you are hosting your project on Github, add the following template to
;; `.github/workflows/gh-pages.yml` to have Github automatically build and
;; deploy your Clerk static build on every push to `main`.

;; ```yml
;; name: GitHub Pages
;;
;; on:
;;   push:
;;     branches:
;;       - main  # Set a branch name to trigger deployment
;; jobs:
;;   publish:
;;     runs-on: ubuntu-20.04
;;     steps:
;;       - name: Cache Maven packages
;;         uses: actions/cache@v3
;;         with:
;;           path: ~/.m2
;;           key: ${{ runner.os }}-m2-${{ hashFiles('**/deps.edn') }}
;;           restore-keys: ${{ runner.os }}-m2
;;
;;       - uses: actions/checkout@v3
;;
;;       - name: Install clojure tools
;;         uses: DeLaGuardo/setup-clojure@4.0
;;         with:
;;           cli: 1.11.1.1208 # Clojure CLI based on tools.deps
;;           github-token: ${{ secrets.GITHUB_TOKEN }}
;;
;;       - name: Install babashka
;;         uses: just-sultanov/setup-babashka@v2
;;         with:
;;           version: '0.8.156'
;;
;;       - name: Build static site
;;         run: bb publish-gh-pages
;;
;;       - name: Deploy
;;         uses: peaceiris/actions-gh-pages@v3
;;         if: ${{ github.ref == 'refs/heads/main' }}
;;         with:
;;           github_token: ${{ secrets.GITHUB_TOKEN }}
;;           publish_dir: ./public
;; ```
;;
;; ### Clerk's Garden
;;
;; To build and host your project with Garden, simply visit
;; `https://github.clerk.garden/$YOUR_ORG/$PROJECT_NAME`, and watch the build
;; process commence.
;;
;; > You'll need to have configured your project using the templates above for
;; > this to work with custom ClojureScript.


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
