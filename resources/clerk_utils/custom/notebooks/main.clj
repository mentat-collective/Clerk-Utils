;; # Welcome to [Clerk](https://clerk.vision/)!

(ns {{top/ns}}.{{main/ns}}
    (:require [nextjournal.clerk :as clerk]))

;; Hello! This project was generated by the [`clerk-utils/custom`
;; template](https://clerk-utils.mentat.org/#project-template). The template
;; comes with everything you need you everything you need to

;; - Interactively develop Clerk notebooks
;; - Publish them to [GitHub Pages](https://pages.github.com/) or [Clerk's Garden](https://github.clerk.garden/)
;; - Use a [custom ClojureScript build](https://clerk-utils.mentat.org/#custom-clojurescript-builds) in both modes

;; Some good next steps:

;; - Visit this project's README.md for guides on how to proceed.
;; - Read the [Book of Clerk](https://book.clerk.vision/)
;; - Visit the [Awesome-Clerk](https://github.com/mentat-collective/awesome-clerk) list for ideas
;; - Install and work with one of the [Clerk plugins](https://clerk-utils.mentat.org/#clerk-plugins)
;; - Delete anything in this notebook you feel like you don't need.

;; ## Clerk Environment Basics
;;
;; Here's a basic use of Clerk. This is a function:

(defn hello [s]
  (str "Hello, " s "!"))

;; If we call this function, Clerk will render the form and its output:

(hello "Clerk")

;; - The form `(hello "Clerk")` executes in the JVM.
;; - The resulting string "Hello, Clerk!" is serialized over a WebSocket to the
;;   browser, where Clerk's ClojureScript side renders it using a
;;   pre-registered "viewer".
;; - These viewers are written in code evaluated using the [Small Clojure
;;   Interpreter](https://github.com/babashka/sci), or "SCI".

;; Clerk comes with [many viewers](https://book.clerk.vision/#viewers), and
;; gives us the ability to [write _new_
;; viewers](https://book.clerk.vision/#writing-viewers) using a default SCI
;; environment that Clerk makes available to us.

;; ### Custom Viewers

;; For example, the following viewer acts on a number, and turns it into a
;; string that tells us a nice mathematical fact:

(def simple-squared-viewer
  {:transform-fn clerk/mark-presented
   :render-fn
   '(fn [x]
      [:pre x "² is equal to " (* x x) "."])})

;; We can call the viewer like so:

(clerk/with-viewer simple-squared-viewer
  12)

;; This is powerful stuff. But what if we want to use something that Clerk
;; hasn't included, like a library from the wide world of JavaScript, or
;; some [Reagent](https://reagent-project.github.io/) component?
;;
;; To do this we'll need to extend Clerk's SCI environment. This project is
;; already set up to do this, as we'll see.

;; ## Custom CLJS
;;
;; Here is a version of `simple-squared-viewer` that uses a custom ClojureScript
;; function `square` defined in `src/{{top/file}}/custom.cljs`:

(def squared-viewer
  {:transform-fn clerk/mark-presented
   :render-fn
   '(fn [x]
      [:pre x "² is equal to " ({{top/ns}}.custom/square x) "."])})

;; Use this new viewer to show that custom ClojureScript works:

(clerk/with-viewer squared-viewer
  12)

;; All ClojureScript code you add to `src/{{top/file}}/custom.cljs` is available
;; for use inside any [custom viewer code you
;; write](https://book.clerk.vision/#writing-viewers).

;; This is made possible by the code in `src/{{top/file}}/sci_viewers.cljs`. If
;; you want to add more namespaces, follow the instructions in
;; `sci_viewers.cljs` to get them into Clerk's SCI environment.

;; `sci_viewers.cljs` also contains instructions on how to make JavaScript and
;; NPM dependencies available to your viewers.
