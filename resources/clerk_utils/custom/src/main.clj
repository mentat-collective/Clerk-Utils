;; # Welcome to Clerk!

(ns {{top/ns}}.{{main/ns}}
    (:require [nextjournal.clerk :as clerk]))

;; This is some Markdown.

(defn hello [s]
  (str "Hello, " s "!"))

(hello "Clerk")

;; ## Custom CLJS

(def squared-viewer
  {:transform-fn clerk/mark-presented
   :render-fn
   '(fn [x]
      [:pre x "² is equal to " (custom/square x) "."])})

;; Use this new viewer to show that custom ClojureScript works:

(clerk/with-viewer squared-viewer
  12)
