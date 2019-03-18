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
            ))

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

(defn noop [])

(set! *main-cli-fn* noop)
