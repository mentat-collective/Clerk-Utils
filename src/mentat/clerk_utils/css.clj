(ns mentat.clerk-utils.css
  "Functions for adding custom CSS to Clerk's header."
  (:refer-clojure :exclude [set!])
  (:require [hiccup.page :as hiccup]
            [nextjournal.clerk.view :as view]))

;; ## CSS Customization

(def custom-css
  "Stateful reference to a sequence of custom CSS sources to be
  included in Clerk's page when it loads."
  (atom []))

(alter-var-root
 #'view/include-viewer-css
 (fn [old]
   (fn [& xs]
     (concat
      (map hiccup/include-css @custom-css)
      (apply old xs)))))

(defn add-css!
  "Takes any number of CSS paths and modifies Clerk to include these CSS files in
  its header.

  Successive calls to [[add-css!]] will append files to the list.
  Call [[reset-css!]] to reset, or [[set-css!]] for a non-appending version."
  [& entries]
  (swap! custom-css into entries))

(defn set-css!
  "Takes any number of CSS paths and modifies Clerk to include these CSS files in
  its header.

  Any call to [[set-css!]] will wipe out any custom CSS already appended.
  Call [[reset-css!]] to reset, or [[set-css!]] for a version that appends files
  on each call."
  [& entries]
  (reset! custom-css (into [] entries)))

(defn reset-css!
  "Resets Clerk's CSS header list to the default."
  []
  (set-css! []))
