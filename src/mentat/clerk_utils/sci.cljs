(ns mentat.clerk-utils.sci)

(defn js->sci-ns [js-namespace]
  (-> (into {}
            (map (fn [[k v]] [(symbol k) v]))
            (.entries js/Object js-namespace))))
