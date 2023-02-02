(ns mentat.clerk-utils.sci
  "Utilities for working with and extending Clerk's SCI environment."
  (:require [nextjournal.clerk.sci-env :as clerk.sci]))

(defn register-js!
  "Given some string `libname` and a JS namespace object (and an optional symbolic
  `alias`), makes the JS namespace available to SCI.

  For example, given a namespace with `(:require [\"react\" :as re])`:

  ```clj
  (register-js! \"react\" re)
  ```

  Makes it possible to call

  ```clj
  (clerk/eval-cljs '(require '[\"react\" :as react]))
  ```

  in a notebook.

  Calling

  ```clj
  (register-js! \"react\" re 'react)
  ```

  makes the `react` alias available to every consumer of the SCI environment."
  ([libname js-namespace]
   (set! clerk.sci/libname->class
         (assoc clerk.sci/libname->class libname js-namespace)))
  ([libname js-namespace alias]
   (register-js! libname js-namespace)
   (when alias
     (clerk.sci/eval-form
      `(~'require '[~libname :as ~alias])))))
