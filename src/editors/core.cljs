(ns editors.core
  (:require [cljs.reader :refer [read-string]]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]
            [cljs.nodejs :as nodejs]
            [cljs-node-io.core :as io :refer [slurp spit]]))

(nodejs/enable-util-print!)

(defn get-version [s]
  (-> s
      (read-string)
      (nth 2)))

(defn get-name [s]
  (-> s
      (read-string)
      (nth 1)
      (str)))

(defn- update-version [s version]
  (-> s
      (z/of-string)
      z/down
      z/right
      z/right
      (z/edit (constantly version))
      z/root-string))

(defn project-dependencies [s]
  (->>
   (-> s
       (z/of-string)
       z/down
       (z/find-next-value :dependencies)
       z/right
       z/sexpr)
   (sort-by (comp name first))
   (map #(conj (rest %) (str (first %))))))

(defn edit-library [s library-name library-version]
  (-> s
      (z/of-string)
      z/down
      (z/find-next-value :dependencies)
      (z/find z/next #(if-let [s (z/sexpr %)]
                        (and (symbol? s)
                             (= library-name (str s)))))
      (z/right)
      (z/edit (constantly library-version))
      (z/root-string)))

(defn has-plugin [s plugin-symbol-name]
  (let [[_ ns-name sym-name] (re-find #"(.*)/(.*)" plugin-symbol-name)]
    (->> s
         (read-string)
         (drop 3)
         (partition 2)
         (filter #(= :plugins (first %)))
         (first)
         (second)
         (filter #(if-let [plugin (first %)]
                    (and
                     (symbol? plugin)
                     (= (namespace plugin) ns-name)
                     (= (name plugin) sym-name))))
         (first))))
