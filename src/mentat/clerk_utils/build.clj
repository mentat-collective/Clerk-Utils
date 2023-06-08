(ns mentat.clerk-utils.build
  "Versions of `nextjournal.clerk/{build!,serve!,halt!} that support custom CLJS
  compilation.`"
  (:require [clojure.java.io :as io]
            [mentat.clerk-utils.docs :refer [git-sha]]
            [mentat.clerk-utils.build.shadow :as shadow]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.analyzer :as analyzer]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.view]
            [nextjournal.clerk.viewer :as cv])
  (:import (java.nio.file Files)))

;; ## Viewer JS Utilities

(def ^:private js-k "/js/viewer.js")

(defn ^:no-doc viewer-js
  "Returns Clerk's currently assigned viewer JS path."
  []
  (@config/!resource->url js-k))

(defn ^:no-doc set-viewer-js!
  "Sets Clerk's viewer JS path to `path`."
  [path]
  (swap! config/!resource->url assoc js-k path))

(defn ^:no-doc reset-viewer-js!
  "Resets Clerk's viewer JS path to its default."
  []
  (let [default (@config/!asset-map js-k)]
    (swap! config/!resource->url assoc js-k default)))

(defn ^:no-doc with-viewer-js
  "Executes the thunk `f` with Clerk's viewer JS viewer set to `path`, and returns
  the value of `(f)`. After execution, returns Clerk's viewer JS to its initial
  value.

  NOTE that this is NOT thread safe, as the viewer is not dynamically bound!
  Please don't try to launch multiple asynchronous calls to [[with-viewer-js]]."
  [path f]
  (let [v (viewer-js)]
    (try (set-viewer-js! path)
         (f)
         (finally
           (set-viewer-js! v)))))

;; ## Custom CLJS

(defn serve!
  "Version of [[nextjournal.clerk/serve!]] that supports custom CLJS compilation
  via `shadow-cljs`.

  In addition to all options supported by Clerk's `serve!`, [[serve!]] supports
  the following options:

  - `:cljs-namespaces`: a sequence of CLJS namespaces to compile and make
    available to Clerk. If provided, [[serve!]] will compile a custom CLJS bundle
    and configure Clerk to use this bundle instead of its default.

  - `:custom-js`: custom JS bundle to use instead of Clerk's built-in JS or a
    custom bundle.

  - `:shadow-options`: these options are forwarded
    to [[mentat.clerk-utils.build.shadow/watch!]]. See that function's docs for
    more detail.

    This bundle is served from a running shadow-cljs server and recompiled when
    any dependency or namespace changes. Defaults to `nil`.

  The only other difference is that if `(:browse? opts)` is `true`, [[serve!]]
  calls [[nextjournal.clerk/show!]] automatically on `(:index opts)` if
  provided.

  All remaining `opts` are forwarded to [[nextjournal.clerk/serve!]]."
  [{:keys [cljs-namespaces custom-js browse? index] :as opts}]
  (when (and cljs-namespaces custom-js)
    (throw
     (AssertionError.
      "Specify only one of `:cljs-namespaces` or `:custom-js`.")))

  (when (seq cljs-namespaces)
    (let [{:keys [js-url]} (shadow/watch! cljs-namespaces)]
      (set-viewer-js! js-url)))

  (when custom-js
    (set-viewer-js! custom-js))

  (when (and browse? index)
    (clerk/show! index))

  (clerk/serve! opts))

(defn halt!
  "Version of [[nextjournal.clerk/halt!]] that additionally kills any shadow-cljs
  processes, if they are running."
  []
  (shadow/stop-watch!)
  (reset-viewer-js!)
  (clerk/halt!))

;; This shutdown hook ensures good resource cleanup in the case of a sudden
;; process shutdown.
(-> (Runtime/getRuntime)
    (.addShutdownHook
     (Thread.
      (fn []
        (println "Calling `mentat.clerk-utils.build/halt!` on shutdown...")
        halt!))))

(defn build!
  "Version of [[nextjournal.clerk/build!]] that supports custom CLJS compilation.

  In addition to all options supported by Clerk's `build!`, [[build!]] supports
  the following options:

  - `:cljs-namespaces`: a sequence of CLJS namespaces to compile and make
    available to Clerk. If provided, [[build!]] will compile a custom CLJS bundle
    and configure Clerk to use this bundle instead of its default. Defaults to
    `nil`.

  - `:custom-js`: custom JS bundle to use instead of Clerk's built-in JS or a
    custom bundle.

  - `:cname`: string denoting the custom hostname from which the site will be
    served. If provided, [[build!]] will create a `CNAME` file containing the
    value in `(:out-path opts)`. Defaults to `nil`.

  The only other difference is that [[build!]] populates `:git/sha` if it hasn't
  been provided.

  All remaining `opts` are forwarded to [[nextjournal.clerk/build!]]"
  [{:keys [cljs-namespaces custom-js out-path cname]
    :or {out-path "public/build"}
    :as opts}]
  (when (and cljs-namespaces custom-js)
    (throw
     (AssertionError.
      "Specify only one of `:cljs-namespaces` or `:custom-js`.")))

  (let [sha    (or (:git/sha opts) (git-sha))
        !build (delay
                 (clerk/build!
                  (assoc opts
                         :git/sha sha
                         :out-path out-path)))]
    (try
      (cond (seq cljs-namespaces)
            (let [js-path (shadow/release! cljs-namespaces)
                  cas     (->> (.toPath (io/file js-path))
                               (Files/readAllBytes)
                               (cv/store+get-cas-url!
                                {:out-path out-path
                                 :ext "js"}))]
              ;; This is necessary for folders with underscores to work on GitHub Pages,
              ;; like the one that Clerk uses to store data for its CAS.
              (spit (str out-path "/.nojekyll") "")
              (with-viewer-js cas
                (fn [] @!build)))

            custom-js
            (with-viewer-js custom-js
              (fn [] @!build))

            :else @!build)
      (finally
        (when cname
          (spit (str out-path "/CNAME") cname))))))

(defn release->cas!
  "Builds a custom JavaScript bundle with the contents of `cljs-namespaces` (and
  any Clerk-required namespaces) and attempts to upload the file to Clerk's CAS.
  p
  Returns the CDN path of the uploaded bundle.

  To use this [[release->cas!]] you'll need a dependency
  on [nextjournal/cas-client](https://github.com/nextjournal/cas-client), like

  ```clojure
  io.github.nextjournal/cas-client {:git/sha \"84ab35c3321c1e51a589fddbeee058aecd055bf8\"}
  ```

  Arguments:

  - `:cljs-namespaces`: a sequence of (symbols representing) CLJS namespaces to
    compile and make available to Clerk. If `nil`, `release->cas!` will do
    nothing.

  - `:prefix`: if supplied, [[release->cas!]] will tag the file as
    `<prefix>@<hash>`, else just as `<hash>`.

  - `:cas-namespace`: an organization or username associated with `:token`.

  - `:token`: [GitHub classic
    token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)
    with `read:user` permissions if `:cas-namespace` equals your GitHub username,
    or `read:org` permissions if `:cas-namespace` equals an organization for which
    you have permissions.

   Create a new token [here](https://github.com/settings/tokens/new)."
  [{:keys [cljs-namespaces prefix token cas-namespace]}]
  (when (seq cljs-namespaces)
    (if-let [put! (requiring-resolve 'nextjournal.cas-client/cas-put)]
      (let [js-path (shadow/release! cljs-namespaces)
            hash (-> (io/file js-path)
                     (.toPath)
                     (Files/readAllBytes)
                     (analyzer/sha2-base58))
            tag (if prefix
                  (str prefix "@" hash)
                  hash)]
        (-> (put! {:path js-path
                   :auth-token token
                   :namespace cas-namespace
                   :tag tag})
            (get-in ["manifest" js-path])))
      (prn "`nextjournal.cas-client` not found on the classpath."))))
