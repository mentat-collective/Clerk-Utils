(ns {{top/ns}}.custom)

;; With the default configuration in `{{top/ns}}.sci-extensions`, any function
;; or var you add here will be available inside your SCI viewers as
;; `custom/<fname>` or as the fully-qualified `{{top/ns}}.custom/<fname>`.

(defn square
  "Returns the square of `x`."
  [x]
  (* x x))
