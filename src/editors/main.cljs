(ns editors.main
  (:require [editors.core :as core]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs.analyzer :as cljs]
            [cljs.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]
            [editors.cljfmt :as cljfmt]
            [atomist.cljs-log :as log]
            [atomist.encrypt :as encrypt]
            [http.util :as util]
            [goog.string :as gstring]
            [goog.string.format]
            [atomist.goals :as goals]
            [atomist.promise :as promise]
            [atomist.logback :as logback]
            [hasch.core :as hasch]
            [atomist.fingerprints :as fingerprints]
            [atomist.public-defns :as public-defns]
            [atomist.lein :as lein]
            [atomist.maven :as maven]
            [atomist.json :as json]))

(defn edit-file [f editor & args]
  (spit f (apply editor (slurp f) args)))

(defn ^:export setVersion [f version]
  (js/Promise.
   (fn [resolve reject]
     (try
       (if version
         (resolve (edit-file f core/update-version version))
         (reject "setVersion was called with a null version parameter"))
       (catch :default t
         (log/warn "unable to run setVersion " (str t))
         (reject t))))))

(s/fdef setVersion
        :args (s/cat :file string? :version string?))

(defn ^:export getName [f]
  (-> (slurp f)
      (core/get-name)))

(s/fdef getName
        :args (s/cat :file string?))

(defn ^:export getVersion [f]
  (-> (slurp f)
      (core/get-version)))

(s/fdef getVersion
        :args (s/cat :file string?))

(defn ^:export projectDeps [f]
  (-> (slurp f)
      (core/project-dependencies)
      clj->js))

(s/fdef projectDeps
        :args (s/cat :file string?))

(defn ^:export cljfmt [f]
  (js/Promise.
   (fn [resolve reject]
     (try
       (log/info "run cljfmt on " f)
       (resolve (cljfmt/cljfmt f))
       (catch :default e
         (log/warn "failure to run cljfmt " e)
         (reject e))))))

(defn ^:export hasLeinPlugin [f p]
  (-> (slurp f)
      (core/has-plugin p)
      (boolean)))
(s/fdef hasLeinPlugin
        :args (s/cat :file string? :plugin string?))

(defn ^:export vault [key f]
  (clj->js
   (encrypt/vault-contents key f)))

(s/fdef cljfmt
        :args (s/cat :file string?))

(defn ^:export updateProjectDep [f libname version]
  (edit-file f core/edit-library libname version))

(defn ^:export readVault [f1 f2]
  (clj->js
   (encrypt/read-vault f1 f2)))

(defn ^:export createKey []
  (log/info "creating a key in key.txt")
  (encrypt/generate-key))

(defn ^:export mergeVault
  [f1 f2 s]
  (encrypt/merge-vault f1 f2 (js->clj (util/json-decode s))))

;; ------------------------------------
;; ------------------------------------

(defn ^:export checkFingerprintTargets
  "check a project for whether it's dependencies are aligned with the current goals

   returns Promise<boolean>"
  [pref-query send-message confirm-goal diff]
  (promise/chan->promise
   (goals/check-fingerprint-goals pref-query send-message confirm-goal (js->clj diff :keywordize-keys true))))

(defn ^:export broadcastFingerprint
  "use fingerprints to scan for projects that could be impacted by this new lib version

   returns Promise<any>"
  [fingerprint-query fp cb]
  (promise/chan->promise
   (goals/broadcast-fingerprint fingerprint-query (js->clj fp :keywordize-keys true) cb)))

(defn ^:export voteResults
  [votes]
  (let [vs (-> votes
               (js->clj :keywordize-keys true)
               (->> (filter #(and (map? %) (:decision %)))))]
    (clj->js {:failed (boolean (some #(= "Against" (:decision %)) vs))
              :failedFps (->> vs
                              (filter #(= "Against" (:decision %)))
                              (map :name))
              :successFps (->> vs
                               (filter #(= "For" (:decision %)))
                               (map :name))
              :failedVotes (->> vs
                                (filter #(= "Against" (:decision %)))
                                (into []))})))

(defn ^:export partitionByFeature
  [fps callback]
  (let [partitioned (->> (js->clj fps :keywordize-keys true)
                         (map (fn [fp] (assoc fp
                                         :type (or (:type fp) (:name fp))
                                         :data (json/json-str (:data fp)))))
                         (sort-by :type)
                         (partition-by :type)
                         (map (fn [fp-coll] {:type (-> fp-coll first :type)
                                             :additions (into [] fp-coll)}))
                         (into []))]
    (log/info (with-out-str (pprint partitioned)))
    (js/Promise.
     (fn [resolve reject]
       (try
         (resolve (callback (clj->js partitioned)))
         (catch :default t
           (log/error "Error sending partitioned FPs " (str t))
           (reject t)))))))

(defn ^:export sha256 [s]
  (clj->js (lein/sha-256 (js->clj s))))

;; ------------------------------

(defn ^:export mavenCoordinates
  [s]
  (fingerprints/fingerprint s "pom.xml" maven/coordinates))

(defn ^:export mavenDeps
  [s]
  (fingerprints/fingerprint s "pom.xml" maven/deps))

(defn ^:export leinCoordinates
  [s]
  (fingerprints/fingerprint s "project.clj" lein/coordinates))

(defn ^:export leinDeps
  [s]
  (fingerprints/fingerprint s "project.clj" lein/deps))

(defn ^:export logbackFingerprints
  "generate elk-logback fingerprint"
  [s]
  (logback/fingerprint s))

(defn ^:export cljFunctionFingerprints
  "generate public-defn-bodies fingerprints"
  [s]
  (public-defns/fingerprint s))

(defn ^:export applyFingerprint
  "apply maven.cljs, leiningen dep fingerprints, public defn bodies, and logback fingerprints
   returns Promise<boolean>"
  [basedir fp]
  (promise/chan->promise
   (go
    (let [clj-fp (js->clj fp :keywordize-keys true)]
      (log/info "apply fingerprint " clj-fp " to basedir " basedir)
      ;; currently sync functions but they should probably return channels
      (fingerprints/apply-fingerprint basedir clj-fp)
      (logback/apply-fingerprint basedir clj-fp))
    true)))

;; ------------------------------


(defn format-list [xs]
  (->> xs
       (map #(gstring/format "`%s`" %))
       (interpose ",")
       (apply str)))

(defn ^:export renderDiff [diff]
  (log/info "renderDiff" (with-out-str (cljs.pprint/pprint (js->clj diff :keywordize-keys true))))
  (let [event (js->clj diff :keywordize-keys true)
        {:keys [owner repo] {:keys [from to]} :data {fp-name :name} :from} event]
    (if (or from to)
      (gstring/format "%s\n%s/%s %s"
                      (str
                       (if from (gstring/format "removed %s" (format-list from)))
                       (if (and from to) ", ")
                       (if to (gstring/format "added: %s" (format-list to))))
                      owner repo fp-name))))

(defn ^:export renderOptions [options]
  (log/info "renderOptions" (with-out-str (cljs.pprint/pprint (js->clj options :keywordize-keys true))))
  (let [event (js->clj options :keywordize-keys true)]
    (with-out-str
     (pprint (->> (seq event)
                  (map (fn [x] [(:text x) (:value x)]))
                  (into {}))))))

(defn ^:export renderData [x]
  (let [event (js->clj x :keywordize-keys true)]
    (with-out-str
     (pprint event))))

(defn ^:export renderProjectLibDiff [diff, target]
  (let [{:as d} (js->clj diff :keywordize-keys true)
        {:as t} (js->clj target :keywordize-keys true)]
    (clj->js
     {:title (gstring/format "New Library Target")
      :description (gstring/format
                    "Target version for library *%s* is *%s*.  Currently *%s* in *%s/%s*"
                    (-> d :to :data (nth 0))
                    (-> t :data (nth 1))
                    (-> d :to :data (nth 1))
                    (-> d :owner)
                    (-> d :repo))})))

(defn ^:export commaSeparatedList [x]
  (let [event (js->clj x :keywordize-keys true)]
    (apply str (interpose "," event))))

(defn ^:export consistentHash [edn]
  (.toString (hasch/uuid5 (hasch/edn-hash (js->clj edn)))))

;; ------------------------------

(defn noop [])

(set! *main-cli-fn* noop)
