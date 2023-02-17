(ns mentat.clerk-utils.viewers-test
  (:require [clojure.test :refer [is deftest testing]]
            [mentat.clerk-utils.viewers :as v]))

(deftest q-tests
  ;; TODO move in the expansion macro!
  (testing "cake"
    (is (= '(fn [x]
              (= 2 (mentat.clerk-utils.viewers/+ x x)))
           (v/q
            (fn [x]
              (= 2 (v/+ x x)))))))  )
