(ns user
  (:require [mentat.clerk-utils.build :as b]))

(def index
  "notebooks/{{top/file}}/{{main/file}}.clj")

(def defaults
  {
   ;; TODO change to "Comment this line" if you want to specify a specific file
   ;; as your index. If it stays commented and you add more notebooks, Clerk
   ;; will automatically generate an index for you.
   :index index
   :browse? true
   :watch-paths ["notebooks"]
   :cljs-namespaces
   '[{{top/ns}}.sci-extensions]})

(def static-defaults
  (assoc defaults
         :browse? false
         :paths ["notebooks/**"]
         ;; Uncomment this if you have a custom cname.
         :cname "{{cname}}"
         :git/url "https://github.com/{{raw-name}}"))

(defn serve!
  "Alias of [[mentat.clerk-utils.build/serve!]] with [[defaults]] supplied as
  default arguments.

  Any supplied `opts` overrides the defaults."
  ([] (serve! {}))
  ([opts]
   (b/serve!
    (merge defaults opts))))

(def ^{:doc "Alias for [[mentat.clerk-utils.build/halt!]]."}
  halt!
  b/halt!)

(defn build!
  "Alias of [[mentat.clerk-utils.build/build!]] with [[static-defaults]] supplied
  as default arguments.

  Any supplied `opts` overrides the defaults."
  [opts]
  (b/build!
   (merge static-defaults opts)))
