(ns mentat.clerk-utils.build
  "Versions of `nextjournal.clerk/{build!,serve!,halt!} that support custom CLJS
  compilation.`"
  (:require [clojure.java.io :as io]
            [mentat.clerk-utils.docs :refer [git-sha]]
            [mentat.clerk-utils.build.shadow :as shadow]
            [nextjournal.clerk :as clerk]
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

  - `:shadow-options`: these options are forwarded
    to [[mentat.clerk-utils.build.shadow/watch!]]. See that function's docs for
    more detail.

    This bundle is served from a running shadow-cljs server and recompiled when
    any dependency or namespace changes. Defaults to `nil`.

  The only other difference is that if `(:browse? opts)` is `true`, [[serve!]]
  calls [[nextjournal.clerk/show!]] automatically on `(:index opts)` if
  provided.

  All remaining `opts` are forwarded to [[nextjournal.clerk/serve!]]."
  [{:keys [cljs-namespaces browse? index] :as opts}]
  (when (seq cljs-namespaces)
    (let [{:keys [js-url]} (shadow/watch! cljs-namespaces)]
      (set-viewer-js! js-url)))
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

  - `:cname`: string denoting the custom hostname from which the site will be
    served. If provided, [[build!]] will create a `CNAME` file containing the
    value in `(:out-path opts)`. Defaults to `nil`.

  The only other difference is that [[build!]] populates `:git/sha` if it hasn't
  been provided.

  All remaining `opts` are forwarded to [[nextjournal.clerk/build!]]"
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
              cas     (->> (.toPath (io/file js-path))
                           (Files/readAllBytes)
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

(require '[nextjournal.clerk.analyzer :as analyzer])

(defn freeze!
  [{:keys [cljs-namespaces]}]
  (when (seq cljs-namespaces)
    (let [js-path (shadow/release! cljs-namespaces)
          data    (->> (.toPath (io/file js-path))
                       (Files/readAllBytes))
          tag     (analyzer/sha2-base58 data)]
      tag)))

(comment
  (let [data (Files/readAllBytes(.toPath (io/file "./template/pom.xml")))
        tag  (analyzer/sha2-base58 data)]
    ((requiring-resolve 'nextjournal.cas-client/cas-put)
     {:path "template"
      :auth-token "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"
      #_(System/getenv "GITHUB_TOKEN")
      :namespace "nextjournal"
      :tag tag})
    ))
