(ns mentat.clerk-utils.build
  (:require [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [clojure.string]
            [hiccup.page :as hiccup]
            [mentat.clerk-utils.shadow :as shadow]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.view]
            [nextjournal.clerk.viewer :as cv]))

;; ## CSS Customization

(def ^:no-doc custom-css
  (atom []))

(defn- rebind [^clojure.lang.Var v f]
  (let [old (.getRawRoot v)]
    (.bindRoot v (f old))))

(defonce _ignore
  (rebind
   #'nextjournal.clerk.view/include-viewer-css
   (fn [old]
     (fn [& xs]
       (concat
        (map hiccup/include-css @custom-css)
        (apply old xs))))))

(defn add-css! [& entries]
  (swap! custom-css into entries))

(defn set-css! [& entries]
  (reset! custom-css (into [] entries)))

(defn reset-css! []
  (set-css! []))

;; ## Utilities

(defn git-sha
  "Returns the sha hash of this project's current git revision."
  []
  (-> (sh "git" "rev-parse" "HEAD")
      (:out)
      (clojure.string/trim)))

(defn git-dependency
  "Nice for display!"
  [slug]
  (clerk/md
   (format
    "```clj

{'io.github.%s
  {:git/sha \"%s\"}}
  ```" slug (git-sha))))

(def ^:private js-k "/js/viewer.js")

(defn viewer-js []
  (@config/!resource->url js-k))

(defn set-viewer-js! [path]
  (swap! config/!resource->url assoc js-k path))

(defn reset-viewer-js! []
  (let [default (@config/!asset-map js-k)]
    (swap! config/!resource->url assoc js-k default)))

(defn with-viewer-js [path f]
  (let [v (viewer-js)]
    (try (set-viewer-js! path)
         (f)
         (finally
           (set-viewer-js! v)))))

;; API Clones

(defn serve!
  "Like `clerk/serve!` but handles custom cljs. If `browse?` and `index` are true,
  shows the entry at `show!` once it opens."
  [{:keys [cljs-namespaces browse? index] :as opts}]
  (when (seq cljs-namespaces)
    (let [{:keys [js-path]} (shadow/watch! cljs-namespaces)]
      (set-viewer-js! js-path)))
  (try (clerk/serve! opts)
       (finally
         (when (and browse? index)
           (clerk/show! index)))))

(defn halt!
  "like `clerk/halt!` but stops the shadow watcher if it's running."
  []
  (shadow/stop-watch!)
  (reset-viewer-js!)
  (clerk/halt!))

(defn static-build!
  "This task is used to generate static sites for local use, GitHub pages
  deployment and Clerk's Garden CDN.

  Supports the following options:

  - `:cljs-namespaces`: a sequence of namespaces to include. Defaults to `nil`.

  - `:out-path`: directory where Clerk will generate the HTML for its static
    site. Defaults to \"public/build\".

  - `:git/sha`, `:git/url`: add these for each generated Clerk page to include a
    reference back to the GitHub page of the file that generated it. `:git/sha`
    defaults to `([[git-sha]])`.

  Given these options, runs the following tasks:

  - Triggers a shadow-cljs release build including all `:cljs-namespaces`,
    downloading all required deps via npm. The output is stored
    at `(str (:out-path opts) \"/_data/main.js\")`.

  - Configures Clerk to generate files that load the js from the generated name
    above, located at (when deployed) `\"/_data/main.js\"`

  - Generates a static build into `(:out-path opts)`.

  All remaining `opts` are forwarded to [[nextjournal.clerk/build!]]."
  [{:keys [cljs-namespaces out-path cname]
    :or {out-path "public/build"}
    :as opts}]
  (let [sha    (or (:git/sha opts) (git-sha))
        !build (delay
                 (clerk/build!
                  (assoc opts
                         :git/sha sha
                         :out-path out-path)))]
    (try
      (if-not (seq cljs-namespaces)
        @!build
        (let [js-path (shadow/release! cljs-namespaces)
              cas     (->> (fs/read-all-bytes js-path)
                           (cv/store+get-cas-url!
                            {:out-path out-path
                             :ext "js"}))]
          ;; This is necessary for folders with underscores to work on GitHub Pages,
          ;; like the one that Clerk uses to store data for its CAS.
          (spit (str out-path "/.nojekyll") "")
          (with-viewer-js cas
            (fn [] @!build))))
      (finally
        (when cname
          (spit (str out-path "/CNAME") cname))))))
