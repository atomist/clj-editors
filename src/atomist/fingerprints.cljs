(ns atomist.fingerprints
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.data]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs-node-io.file :as file]
            [atomist.json :as json]
            [atomist.cljs-log :as log]
            [atomist.lein :as lein]
            [atomist.maven :as maven]
            [atomist.promise :refer [from-promise]]
            [cljs.pprint :refer [pprint]]
            [cljs.core.async :refer [chan <! >!]]
            [cljs.test :refer-macros [deftest testing is run-tests async] :refer [report testing-vars-str empty-env get-current-env]]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs.spec.alpha :as spec]
            [atomist.specs :as schema]
            [atomist.public-defns :as public-defns]))

(defn- get-file [basedir path f]
  (if-let [file (io/file basedir path)]
    (if (.exists file)
      (f file))))

(defn fingerprint
  "extract library fingerprint data from a basedir containing some sort of project manifest and possibly
   a project lock file (depending on the system)

   returns promise of javascript Fingerprint[] or error "
  [basedir f fingerprinter]
  (js/Promise.
   (fn [accept reject]
     (accept
      (let [data (get-file basedir f fingerprinter)]
        (->> data
             (map #(assoc %
                     :sha (lein/sha-256 (json/json-str (:data %)))
                     :data (:data %)
                     :value (json/json-str (:data %))))
             (into [])
             (clj->js)))))))

(defn apply-fingerprint
  "runs synchronously right now"
  [basedir {:keys [type] :as fingerprint}]

  (get-file
   basedir "pom.xml"
   (fn [f] (maven/apply-fingerprint f fingerprint)))

  (get-file
   basedir "project.clj"
   (fn [f]
     (cond
       (= "clojure-project-deps" type)
       (spit f (lein/edit-library (slurp f) (-> fingerprint :data (nth 0)) (-> fingerprint :data (nth 1)))))))

  (if (= "public-defn-bodies" type)
    (public-defns/apply-fingerprint basedir fingerprint)))
