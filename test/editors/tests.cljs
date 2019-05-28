(ns editors.tests
  (:require [cljs.test :refer-macros [run-tests run-all-tests]]
            [editors.core-t]
            [doo.runner :refer-macros [doo-tests]]))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(doo-tests
 'editors.core-t)

(comment
  (run-all-tests))