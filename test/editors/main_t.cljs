(ns editors.main-t
  (:require [cljs.test :refer-macros [deftest testing is run-tests async]]
            [editors.core :as core]
            [editors.main :as main]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs.pprint :refer [pprint]]))

(defn- always-accept []
  (js/Promise.
   (fn [accept _]
     (accept nil))))

(deftest partitioning-tests
  (async done
    (let [called (atom false)
          callback (fn [partitioned]
                     (is (= [{:type "x"
                              :additions [{:name "x" :sha "y" :type "x" :data "{\"x\":\"y\"}"}]}] (js->clj partitioned :keywordize-keys true)))
                     (println (js->clj partitioned :keywordize-keys true))
                     (reset! called true)
                     (always-accept))
          promise (main/partitionByFeature [{:name "x" :sha "y" :data {:x :y}}] callback)]
      (.then promise (fn [response] (is @called) (done))))))

(deftest empty-partitioning-tests
  (async done
    (let [called (atom false)
          callback (fn [partitioned]
                     (is (= [] (js->clj partitioned)))
                     (println (js->clj partitioned :keywordize-keys true))
                     (reset! called true)
                     (always-accept))
          promise (main/partitionByFeature [] callback)]
      (.then promise (fn [response] (is @called) (done))))))

(deftest multiple-partitions-test
  (async done
    (let [called (atom 0)
          callback (fn [partitioned]
                     (is (= [{:type "x"
                              :additions [{:name "x" :sha "y" :type "x" :data "{\"x\":\"y\"}"}]}
                             {:type "x1"
                              :additions [{:name "x1" :sha "y" :type "x1" :data "{\"x1\":\"y\"}"}
                                          {:name "x" :sha "y2" :type "x1" :data "{\"x\":\"y2\"}"}]}] (js->clj partitioned :keywordize-keys true)))
                     (println (js->clj partitioned :keywordize-keys true))
                     (swap! called inc)
                     (always-accept))
          promise (main/partitionByFeature [{:name "x" :sha "y" :data {:x :y}}
                                            {:name "x1" :sha "y" :data {:x1 :y}}
                                            {:name "x" :sha "y2" :data {:x :y2} :type "x1"}] callback)]
      (.then promise (fn [response] (is (= 1 @called)) (done))))))

(deftest apply-fingerprint
  (async done
    (let [promise (main/applyFingerprint "/Users/slim/atmhq/bot-service"
                                         #js {:type "clojure-project-deps"
                                              :name "org.clojure::clojure"
                                              :data ["org.clojure/clojure", "1.12.0"]
                                              :sha "a"})]
      (.then promise (fn [response] (done))))))

(run-tests)