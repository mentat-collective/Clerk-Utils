^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns clerk-utils.show
  (:require [mentat.clerk-utils.show :refer [show-cljs]]
            [nextjournal.clerk :as-alias clerk]))

;; # cljc demo.

(show-cljs
 [:div
  [:pre "cake"]
  [:pre "cake"]
  [:pre "cake"]])
