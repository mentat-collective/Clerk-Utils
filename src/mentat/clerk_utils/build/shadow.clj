(ns mentat.clerk-utils.build.shadow
  "Utilities for generating custom Clerk viewer CLJS builds via `shadow-cljs`."
  (:require [clojure.string]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [shadow.cljs.devtools.config :as shadow.config]
            [shadow.cljs.devtools.server :as shadow.server]
            [shadow.cljs.devtools.server.runtime :as runtime]
            [shadow.cljs.devtools.server.util :as shadow.util]
            [shadow.cljs.devtools.server.npm-deps :as npm-deps]
            [shadow.cljs.devtools.api :as shadow.api]))

(def ^:private windows?
  (clojure.string/starts-with?
   (System/getProperty "os.name")
   "Windows"))

(def npm-cmd
  "System-specific NPM command, tuned for Windows or non-Windows."
  (if windows? "npm.cmd" "npm"))

(def shadow-version
  "Shadow version of the currently-loaded `shadow-cljs` dependency."
  (shadow.util/find-version))

(def shadow-npm-dep
  "Schema for the NPM dependency for `shadow-cljs` associated with the
  currently loaded `shadow-cljs` JVM library."
  {:id "shadow-cljs"
   :version shadow-version})

(def ^:no-doc build-id ::clerk)

(def output-dir
  "Output directory for our controlled `shadow-cljs` build."
  ".clerk/shadow-cljs")

(def js-path
  "Location of the generated JS code."
  (str output-dir "/main.js"))

(defn clerk-entries
  "Returns the sequence of (symbols representing) cljs namespaces required by the
  loaded version of Clerk for interactive development and static publication."
  []
  (cond-> ['nextjournal.clerk.sci-env]
    (io/resource "nextjournal/clerk/static_app.cljs")
    (conj 'nextjournal.clerk.static-app)))

(defn clerk-build-config
  "Given sequence `cljs-namespaces` of symbols naming ClojureScript namspaces,
  returns a `:builds` entry for a `shadow-cljs` build of Clerk's viewer JS.

  The resulting build will include all supplied namespaces, plus Clerk's
  `nextjournal.clerk.static-app` namespace.`"
  [cljs-namespaces]
  (let [entries (into (clerk-entries) cljs-namespaces)]
    {:build-id build-id
     :target :esm
     :runtime :browser
     :modules {:main {:entries entries}}
     :output-dir output-dir
     :compiler-options
     {:infer-externs :auto :optimizations :advanced}
     :js-options {:output-feature-set :es8}}))

(defn server-config
  "Returns a full, normalized `shadow-cljs` config with no `:builds` registered
  beyond the defaults.

  This config is sufficient to start a `shadow-cljs` server."
  [m]
  (-> (shadow.config/normalize m)
      (->> (merge shadow.config/default-config))
      (update :builds #(merge shadow.config/default-builds %))
      (assoc :user-config (shadow.config/load-user-config))))

(defn install-npm-deps!
  "This command:

  - Triggers an `npm install`
  - Installs any dependency referenced by a `deps.cljs` file on the classpath
  - Installs these dependencies into the calling project's `package.json` file.

  Unlike `shadow-cljs`'s native `npm-deps/main`, this command also
  installs [[shadow-npm-dep]], negating any need to tell the user to have
  versions match or to remember to do this install themselves."
  []
  (let [package-json (npm-deps/read-package-json ".")
        deps         (->> (npm-deps/get-deps-from-classpath)
                          (cons shadow-npm-dep)
                          (npm-deps/resolve-conflicts)
                          (remove #(npm-deps/is-installed? % package-json)))]
    (when (seq package-json)
      (println "Running npm install...")
      (println
       (:out
        (sh npm-cmd "install"))))

    (npm-deps/install-deps {} deps)))

(defn stop-watch!
  "Shuts down the `shadow-cljs` server (if running) and stops the build watcher
  for the custom Clerk viewer build."
  []
  (when (runtime/get-instance)
    (shadow.api/stop-worker build-id)
    (shadow.server/stop!)))

(defn watch!
  "Given some sequence `cljs-namespaces` of symbols representing ClojureScript
  namespaces,

  - installs all required npm dependencies with [[install-npm-deps!]]
  - builds a custom Clerk viewer bundle with these namespaces included - starts
  a `shadow-cljs` watcher process that recompiles the bundle on any file change
  - serves the generated JS from a `shadow-cljs` server.

  Optionally takes a map of config options:

  - `:port`: overrides the port from which `shadow-cljs` serves the JS.

  Returns a map with the following keys:

  - `:js-url`: URL with the JS location
  - `:stop-fn`: No-arg function to call to kill the watcher. (You can also kill
    it with [[stop-watch!]].)"
  ([cljs-namespaces]
   (watch! cljs-namespaces {}))
  ([cljs-namespaces {:keys [port] :or {port 8765}}]
   (let [config       (server-config {:dev-http {port output-dir}})
         build-config (clerk-build-config cljs-namespaces)]
     (install-npm-deps!)
     (shadow.server/start! config)
     (if (shadow.api/worker-running? build-id)
       (prn "Watcher already running!")
       (shadow.api/with-runtime
         (shadow.api/watch build-config)))
     {:js-url (str "http://localhost:" port "/main.js")
      :stop-fn stop-watch!})))

(defn release!
  "Given some sequence `cljs-namespaces` of symbols representing ClojureScript
  namespaces,

  - installs all required npm dependencies with [[install-npm-deps!]]
  - generates a release build of custom Clerk viewer JS.

  Returns the path of the generated JS."
  [cljs-namespaces]
  (let [config (clerk-build-config cljs-namespaces)]
    (install-npm-deps!)
    (shadow.api/with-runtime
      (shadow.api/release* config {})
      nil)
    js-path))
