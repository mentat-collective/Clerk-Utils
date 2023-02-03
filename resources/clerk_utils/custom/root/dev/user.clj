(ns user
  (:require [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [clojure.string]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.viewer :as cv]
            [shadow.cljs.devtools.server :as shadow.server]
            [shadow.cljs.devtools.server.npm-deps :as npm-deps]
            [shadow.cljs.devtools.api :as shadow.api]))

(def index
  "src/{{top/file}}/{{main/file}}.clj")

;; TODO uncomment this if you want to specify a specific file as your index.
;; If it stays commented and you add more notebooks, Clerk will
;; automatically generate an index for you.
;; :index

(def defaults
  {:index index
   :browse? true
   :watch-paths ["src"]
   :cljs-namespaces
   '[clerk-utils.sci-extensions
     clerk-utils.show]})

(def static-defaults
  (assoc defaults
         :browse? false
         :paths ["src/**"]
         :cname "clerk-utils.mentat.org"
         :git/url "https://github.com/mentat-collective/clerk-utils"))

(def build-target
  {:git/url "https://github.com/{{raw-name}}"})

(def ^{:doc "static site defaults for local and github-pages modes."}
  defaults
  {:out-path   "public"
   :cas-prefix "/"})

(defn start!
  "Starts a Clerk server process "
  ([] (start! {}))
  ([opts]
   (let [defaults {:port {{port}}
                   :browse? true}]
     (clerk/serve!
      (merge defaults opts))
     (Thread/sleep 1000)
     (clerk/show! "src/{{top/file}}/{{main/file}}.clj"))))

(defn start-with-shadow!
  ([] (start-with-shadow! {}))
  ([opts]
   (npm-deps/main {} {})
   (shadow.server/start!)
   (shadow.api/watch :clerk)
   (swap! config/!resource->url
          assoc
          "/js/viewer.js"
          "http://localhost:{{shadow-port}}/js/main.js")
   (start! opts)))

(defn git-sha
  "Returns the sha hash of this project's current git revision."
  []
  (-> (sh "git" "rev-parse" "HEAD")
      (:out)
      (clojure.string/trim)))

(defn static-build!
  "This task is used to generate static sites for local use, github pages
  deployment and Clerk's Garden CDN.

  Accepts a map of options `opts` and runs the following tasks:

  - Slurps the output of the shadow-cljs `:clerk` build from `public/js/main.js`
    and pushes it into a CAS located at `(str (:out-path opts) \"/js/_data\")`.

  - Configures Clerk to generate files that load the js from the generated name
    above, stored in `(str (:cas-prefix opts) \"/js/_data\")`

  - Creates a static build of the single namespace [[index]] at `(str (:out-path
    opts) \"/index.html\")`

  All `opts` are forwarded to [[nextjournal.clerk/build!]]."
  [opts]
  (npm-deps/main {} {})
  (shadow.api/release! :clerk)
  (let [{:keys [out-path cas-prefix]} (merge defaults opts)
        sha (or (:git/sha opts) (git-sha))
        cas (cv/store+get-cas-url!
             {:out-path (str out-path "/js") :ext "js"}
             (fs/read-all-bytes "public/js/main.js"))]
    (swap! config/!resource->url assoc
           "/js/viewer.js"
           (str cas-prefix "js/" cas))
    (clerk/build!
     (merge build-target
            (assoc opts
                   :out-path out-path
                   :git/sha sha)))))

(defn garden!
  "Standalone executable function that runs [[static-build!]] configured for
  Clerk's Garden. See [[garden-defaults]] for customizations
  to [[static-build!]]."
  [opts]
  (let [sha (or (:git/sha opts) (git-sha))]
    (static-build!
     (merge {:cas-prefix (str "/{{raw-name}}/commit/" sha "/")
             :git/sha sha}
            opts))))
