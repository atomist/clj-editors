(ns editors.core-t
  (:require [cljs.test :refer-macros [deftest testing is run-tests async]]
            [editors.core :as core]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs.pprint :refer [pprint]]
            [atomist.lein :as lein]
            [rewrite-clj.zip :as z]))

(deftest version-tests
  (testing "that we can extract the version from a project clj file"
    (is (= "1.1" (#'editors.core/get-version (slurp "test-resources/project1.clj")))))
  (testing "that we can update the version in a project clj file"
    (is (= "11.11" (#'editors.core/get-version (#'editors.core/update-version (slurp "test-resources/project1.clj") "11.11"))))))

(deftest name-tests
  (testing "that we can extract the version from a project clj file"
    (is (= "atomist/test" (#'editors.core/get-name (slurp "test-resources/project1.clj")))))
  (testing "that we can update the version in a project clj file"
    (is (= "atomist/test" (#'editors.core/get-name (#'editors.core/update-version (slurp "test-resources/project1.clj") "11.11"))))))

(def project2-deps '(("cljs-node-io" "0.5.0")
                     ("org.clojure/clojure" "1.8.0")
                     ("org.clojure/clojurescript" "1.9.946")
                     ("rewrite-cljs" "0.4.4")))

(deftest dependency-tests
  (testing "that we can extractgs dependency lists"
    (is (= project2-deps (#'editors.core/project-dependencies (slurp "test-resources/project2.clj"))))))

(deftest plugin-check-tests
  (testing "that we can can check for a plugin being present"
    (is (core/has-plugin (slurp "test-resources/org-project.clj") "com.livingsocial/lein-dependency-check"))))

(deftest remove-library-tests
  (testing "removing nothing"
    (is (=
         (core/remove-library (slurp "test-resources/lein/project1.clj") "core/whateve")
         (slurp "test-resources/lein/project1.clj"))))
  (testing "removals"
    (is (=
         (core/remove-library (slurp "test-resources/lein/project1.clj") "cljs-node-io")
         "(defproject atomist/test \"1.1\"\n            :dependencies [[core/whatever \"1.0\"]])"))
    (is (=
         (core/remove-library (slurp "test-resources/lein/project1.clj") "core/whatever")
         "(defproject atomist/test \"1.1\"\n            :dependencies [[cljs-node-io \"0.5.0\"]])"))))

(deftest edit-library-tests
  (testing "that we don't alter excludes"
    (let [s (lein/edit-library (slurp "test-resources/lein/org-project.clj") "com.fasterxml.jackson.core/jackson-databind" "2.9.9.1")
          deps (lein/lein-deps s)]
      (is (some #(= % '("com.fasterxml.jackson.core/jackson-databind" "2.9.9.1"
                        :exclusions
                        [com.fasterxml.jackson.core/jackson-core])) deps))
      (is (some #(= % '("com.atomist/kafka-lib"
                        "6.0.37"
                        :exclusions
                        [org.clojure/clojure
                         org.slf4j/slf4j-log4j12
                         com.fasterxml.jackson.core/jackson-databind
                         org.clojure/tools.reader])) deps))))
  (testing "that we alter a simple lib"
    (let [s (lein/edit-library (slurp "test-resources/project2.clj")
                               "org.clojure/clojure" "1.10.0")
          deps (lein/lein-deps s)]
      (is (= 4 (count deps)))
      (is (some #(= % '("org.clojure/clojure" "1.10.0")) deps)))))
