(ns mentat.clerk-utils.docs
  "Helpful utilities when writing documentation with Clerk."
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as cs]
            [nextjournal.clerk :as clerk]))

(defn git-sha
  "Returns the sha hash of this project's current git revision."
  []
  (cs/trim
   (:out
    (sh "git" "rev-parse" "HEAD"))))

(defn git-dependency
  "Given a GitHub `slug` (ie, a string of the form `\"org-name/project-name\"`,
  like `\"mentat-collective/clerk-utils\"`), returns a Markdown blob that shows
  the correct `deps.edn` `:deps` entry for the library with that slug.

  The git SHA of the dependency defaults to `([[git-sha]])`, but the optional
  second argument `sha` overrides this."
  ([slug] (git-dependency slug (git-sha)))
  ([slug sha]
   (clerk/md
    (format
     "```clj

{io.github.%s
  {:git/sha \"%s\"}}
  ```" slug sha))))
