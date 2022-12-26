^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.show
  (:require [mentat.clerk-utils.show :refer [show-cljs]]
            [nextjournal.clerk :as-alias clerk]))

;; # show macros
;;
;; ## show-sci
;;
;; TODO move demos from the other page
;;
;; ## show-cljs
;;
;; basic idea:

(show-cljs
 [:div
  [:pre "cake"]
  [:pre "cake"]
  [:pre "cake"]])

;; ### Imports etc

;; ### Exporting functions, interacting with SCI
;;
;; top level
;;
;; ### styling?
