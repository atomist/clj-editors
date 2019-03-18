(require '[clojure.java.io :as io]
         '[cljs.build.api :as b])

(b/build (b/inputs "src")
         {:main       'maven.xml
          :target     :nodejs
          :output-to  "maven.js"
          :output-dir "out"
          :verbose    true
          :npm-deps {:xml-js "1.6.7"}
          :install-deps true
          :optimizations :none
          :closure-warnings {:non-standard-jsdoc :off
                             :global-this :off}})

(System/exit 0)