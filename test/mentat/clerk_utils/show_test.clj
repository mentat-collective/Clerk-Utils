(ns mentat.clerk-utils.show-test
  (:require [clojure.test :refer [is deftest testing]]))

(deftest check-tests
  (testing "numbers"
    (is (= 1 1) "Hi!")))
