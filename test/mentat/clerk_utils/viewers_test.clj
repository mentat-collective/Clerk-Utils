(ns mentat.clerk-utils.viewers-test
  (:require [clojure.core :as c]
            [clojure.test :refer [is deftest]]
            [mentat.clerk-utils.viewers :as v]))

(deftest q-tests
  (let [x 10]
    (is (= '(+ 10 clojure.core/y z 4 5)
           (v/q (+ ~x c/y z ~@[4 5])))
        "splicing works"))

  (is (= '(fn [x]
            (= 2 (mentat.clerk-utils.viewers/+ x x q/y)))
         (v/q
          (fn [x]
            (= 2 (v/+ x x q/y)))))
      "aliases present in ns get expanded, others get left alone.")

  (is (= '{'x clojure.core/x
           'y #{1 [2 clojure.core/z] (js/Array. clojure.core/y)}}
         (v/q
          {'x c/x
           'y #{1 [2 c/z] (js/Array. c/y)}}))
      "works with various data structures"))
