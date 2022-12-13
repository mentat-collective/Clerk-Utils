(ns user
  (:require [nextjournal.clerk :as clerk]))

(def index
  "dev/clerk_utils/notebook.clj")

(def defaults
  {:out-path "public"})

(defn start! []
  (clerk/serve!
   {:browse? true
    :watch-paths ["dev"]})
  (Thread/sleep 500)
  (clerk/show! index))

(defn github-pages! [opts]
  (let [{:keys [out-path]} (merge defaults opts)]
    (clerk/build!
     (merge {:index index}
            (assoc opts :out-path out-path)))))

;; TODO I don't THINK this needs anything special going on, but check!
(def garden! github-pages!)
