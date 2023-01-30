(ns mentat.clerk-utils.shadow
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string]
            [nextjournal.clerk.view]
            [shadow.cljs.devtools.config :as shadow.config]
            [shadow.cljs.devtools.server :as shadow.server]
            [shadow.cljs.devtools.server.runtime :as runtime]
            [shadow.cljs.devtools.server.util :as shadow.util]
            [shadow.cljs.devtools.server.npm-deps :as npm-deps]
            [shadow.cljs.devtools.api :as shadow.api]))

(def ^{:doc "Shadow version."}
  version
  (shadow.util/find-version))

(def shadow-npm-dep
  {:id "shadow-cljs"
   :version version})

(def build-id ::clerk)

(def ^{:doc "Output directory for our controlled shadow-cljs build."}
  output-dir
  ".clerk/shadow-cljs")

(def ^{:doc "Location of the generated js code."}
  js-path
  (str output-dir "/main.js"))

(defn shadow-config
  "This is a normalized `:builds` entry for a shadow-cljs build that includes what Clerk needs, plus a sequence of `entries`"
  [cljs-namespaces]
  (let [entries (into ['nextjournal.clerk.static-app]
                      cljs-namespaces)]
    {:build-id build-id
     :target :esm
     :runtime :browser
     :modules {:main {:entries entries}}
     :output-dir output-dir
     :build-options
     {:ns-aliases '{nextjournal.devcards nextjournal.devcards-noop}}
     :compiler-options
     {:infer-externs :auto :optimizations :advanced}
     :js-options {:output-feature-set :es8}}))

(defn server-config
  "This is the FULL normalized shadow-cljs config, not including any builds. We
  need this to start a server."
  [m]
  (-> (shadow.config/normalize m)
      (->> (merge shadow.config/default-config))
      (update :builds #(merge shadow.config/default-builds %))
      (assoc :user-config (shadow.config/load-user-config))))

(defn npm-deps!
  "This triggers `npm install` , AND triggers shadow-cljs to attempt to install
  all deps from the classpath.

  If you only do the second one, then nothing will happen if `package.json`
  includes all correct entries but no install has happened yet."
  []
  (let [package-json (npm-deps/read-package-json ".")
        deps         (->> (npm-deps/get-deps-from-classpath)
                          (npm-deps/resolve-conflicts)
                          (remove #(npm-deps/is-installed? % package-json))
                          (cons shadow-npm-dep))]
    (when (seq package-json)
      (println "Running npm install...")
      (println (sh "npm" "install")))

    (npm-deps/install-deps {} deps)))

(defn stop-watch!
  "Shuts down the running server and stops the build watcher."
  []
  (when (runtime/get-instance)
    (shadow.api/stop-worker build-id)
    (shadow.server/stop!)))

(defn watch!
  "Starts a shadow-cljs process that builds JS with `cljs-namespaces`. Returns a
  map of `:js-path` with the JS location, and `:stop-fn` with a function to call
  to kill the watcher.

  You can also kill it with [[stop-watch!]]"
  ([cljs-namespaces]
   (watch! cljs-namespaces {}))
  ([cljs-namespaces {:keys [port] :or {port 8765}}]
   (let [config       (server-config {:dev-http {port output-dir}})
         build-config (shadow-config cljs-namespaces)]
     (npm-deps!)
     (shadow.server/start! config)
     (if (shadow.api/worker-running? build-id)
       (prn "Watcher already running!")
       (shadow.api/with-runtime
         (shadow.api/watch build-config)))
     {:js-path (str "http://localhost:" port "/main.js")
      :stop-fn stop-watch!})))

(defn release!
  "Generates a release build. Returns the path."
  [cljs-namespaces]
  (let [config (shadow-config cljs-namespaces)]
    (npm-deps!)
    (shadow.api/with-runtime
      (shadow.api/release* config {})
      nil)
    js-path))
