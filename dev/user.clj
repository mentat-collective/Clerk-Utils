(ns user
  (:require [mentat.clerk-utils.build :as b]))

(def index
  "dev/clerk_utils/notebook.clj")

(def defaults
  {:index index
   :browse? true
   :watch-paths ["dev"]
   :cljs-namespaces '[clerk-utils.sci-extensions
                      clerk-utils.show]})

(def static-defaults
  (assoc defaults
         :browse? false
         :paths ["dev/clerk_utils/show.cljc"]
         :cname "clerk-utils.mentat.org"
         :git/url "https://github.com/mentat-collective/clerk-utils"))

(defn serve!
  "Alias of [[mentat.clerk-utils.build/serve!]] with [[defaults]] supplied as
  default arguments.

  Any supplied `opts` overrides the defaults."
  ([] (serve! {}))
  ([opts]
   (b/serve!
    (merge defaults opts))))

(def halt!
  "Alias for [[mentat.clerk-utils.build/halt!]]."
  b/halt!)

(defn build!
  "Alias of [[mentat.clerk-utils.build/build!]] with [[static-defaults]] supplied as
  default arguments.

  Any supplied `opts` overrides the defaults."
  [opts]
  (b/build!
   (merge static-defaults opts)))
