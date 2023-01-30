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

(defn reset-css! []
  (reset! custom-css []))

(defn add-css! [& entries]
  (swap! custom-css into entries))

;; TODO

;; - test the watcher, see if local file changes get picked up
;; -

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

(defn replace-sha-template!
  "Given some `path`, modifies the file at `path` replaces any occurence of the
  string `$GIT_SHA` with the actual current sha of the repo."
  ([path]
   (replace-sha-template! path (git-sha)))
  ([path sha]
   (-> (slurp path)
       (clojure.string/replace "$GIT_SHA" sha)
       (->> (spit path)))))

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

(defn slug->url [slug]
  (str "https://github.com/" slug))

(defn replace-sha [s sha]
  (if (string? s)
    (clojure.string/replace s "$GIT_SHA"  sha)
    s))

(defn get-subfolder [{:keys [github-slug garden? subfolder]} sha]
  (-> (or subfolder
          (when (and garden? github-slug)
            (str github-slug "/commit/$GIT_SHA/")))
      (replace-sha sha)))

;; API Clones

(defn serve!
  "Like `clerk/serve!` but handles custom cljs. If `browse?` and `show` are true,
  shows the entry at `show!` once it opens."
  [{:keys [cljs-namespaces browse? index] :as opts}]
  (when (seq cljs-namespaces)
    (let [{:keys [js-path]} (shadow/watch! cljs-namespaces)]
      (set-viewer-js! js-path)))
  (try (clerk/serve! opts)
       (finally
         (when (and browse? index)
           (Thread/sleep 500)
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

  - `:cljs-namespaces`: a list of namespaces to include. Defaults to `[]`.

  - `:github-slug`: like `\"mentat-collective/clerk-utils\"`.

  - `:garden?` If true, tunes the default `subfolder`.

  - `:subfolder`: path after the top-level domain where the site will be hosted.
    `https://mydomain.com/<subfolder>`, for example. Defaults to \"\". NOTE if you
    include the string $GIT_SHA in the path, it will be replaced with the current
    git sha.

  - `:out-path`: directory where Clerk will generate the HTML for its static
    site. Defaults to \"public\".

  - `:git/sha`, `:git/url`: add these for each generated Clerk page to include a
    reference back to the GitHub page of the file that generated it. `:git/sha`
    defaults to `([[git-sha]])`.

  Given these options, runs the following tasks:

  - Triggers a shadow-cljs release build including all `cljs-namespaces`,
    downloading all required deps via npm. The output is stored
    at `(str (:out-path opts) \"/_data\")`.

  - Configures Clerk to generate files that load the js from the generated name
    above, located at (when deployed) `(str (:sub opts) \"/_data\")`

  - Generates a static build into `(:out-path opts)`.

  All remaining `opts` are forwarded to [[nextjournal.clerk/build!]]."
  [{:keys [cljs-namespaces out-path github-slug]
    :or {out-path "public"
         cljs-namespaces []}
    :as opts}]
  (let [sha       (or (:git/sha opts) (git-sha))
        url       (or (:git/url opts)
                      (when github-slug
                        (slug->url github-slug)))
        subfolder (get-subfolder opts sha)
        !build    (delay
                    (clerk/build!
                     (assoc opts
                            :out-path out-path
                            :git/sha sha
                            :git/url url)))]
    (if-not (seq cljs-namespaces)
      @!build
      (let [js-path (shadow/release! cljs-namespaces)
            cas     (->> (fs/read-all-bytes js-path)
                         (cv/store+get-cas-url!
                          {:out-path out-path
                           :ext "js"}))]
        (with-viewer-js (str subfolder "/" cas)
          (fn [] @!build))))))

(defn garden!
  "Standalone executable function that runs [[static-build!]] configured for
  Clerk's Garden."
  [opts]
  (assert (:github-slug opts) "`:github-slug` is required for a Garden build.")
  (static-build!
   (assoc opts
          :garden? true
          :subfolder nil)))
