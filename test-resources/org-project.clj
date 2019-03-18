(defproject org "0.2.0-SNAPSHOT"
  :description "Authz service"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.9.1"]
                 [clj-time "0.15.1"]
                 [com.atomist/clj-config "17.0.8-20190129102220" :exclusions [commons-logging org.clojure/clojure org.slf4j/slf4j-log4j12 commons-codec]]
                 [com.atomist/clj-token "2.0.0-20190208145539"]
                 [com.atomist/clj-utils "0.0.8"]
                 [com.atomist/kafka-lib "6.0.27" :exclusions [org.clojure/clojure
                                                              org.slf4j/slf4j-log4j12
                                                              com.fasterxml.jackson.core/jackson-databind
                                                              org.clojure/tools.reader]]
                 [com.atomist/metrics "0.1.9-20190225132956"]
                 [com.atomist/threadpool "0.1.2"]
                 [com.climate/claypoole "1.1.4"]
                 [com.taoensso/faraday "1.9.0"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.349" :exclusions [joda-time]]
                 [diehard "0.7.0"]
                 [atomist/saml20-clj "0.1.14-dev.20190104135351" :exclusions [compojure org.clojure/data.codec]]
                 [metosin/compojure-api "1.1.12"]
                 [org.clojure/core.memoize "0.7.1"]
                 [org.clojure/core.async "0.4.490" :exclusions [org.clojure/tools.reader]]
                 [prismatic/schema "1.1.10"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [tentacles "0.5.1" :exclusions [org.clojure/data.codec]]
                 [cheshire "5.8.1"]
                 [clj-statsd "0.4.0"]
                 [amperity/vault-clj "0.6.5"]

                 [com.jeaye/ring-gatekeeper "2018.01.12" :exclusions [riddley org.bouncycastle/bcprov-jdk15on
                                                                      com.taoensso/encore com.taoensso/nippy com.taoensso/truss com.taoensso/timbre
                                                                      com.couchbase.client/java-client]]

                 [mount "0.1.16"]
                 [digest "1.4.8"]

                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [clout "2.2.1"]

                 [io.replikativ/hasch "0.3.5" :exclusions [org.clojure/tools.reader]]

                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [io.logz.logback/logzio-logback-appender "1.0.17"]
                 [io.clj/logging "0.8.1"]
                 [clj-statsd "0.4.0"]

                 [com.fasterxml.jackson.core/jackson-databind "2.9.8" :exclusions [com.fasterxml.jackson.core/jackson-core]]]

  :exclusions [commons-logging log4j org.slf4j/slf4j-log4j12 clj-http clj-time digest
               com.fasterxml.jackson.core/jackson-databind
               environ]

  :test-selectors {:focused :focused
                   :github-integration :github-integration
                   :swagger :swagger
                   :default (complement (fn [m] (some identity ((juxt :github-integration :focused :swagger) m))))}

  :min-lein-version "2.6.1"

  :aot [atomist.org.main]
  :main atomist.org.main

  :plugins [[lein-metajar "0.1.1"]
            [lein-dynamodb-local "0.2.10"]
            [com.livingsocial/lein-dependency-check "1.1.2"]]

  :dependency-check {:properties-file "dependency-check.properties"
                     :suppression-file "suppressions.xml"}

  :container {:name "org"
              :dockerfile "/docker"
              :hub "sforzando-dockerv2-local.jfrog.io"}

  :jar-name "org-service.jar"
  :dynamodb-local {:port 6798
                   :in-memory? true
                   :shared-db? true}

  :profiles {:metajar {:direct-linking true
                       :aot :all}

             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]
                                  [clj-local-secrets "0.5.1"]
                                  [clj-http-fake "1.0.3"]]
                   :repl-options {:init-ns user}
                   :source-paths ["dev/clj"]
                   :resource-paths ["dev/resources"]}

             :integration {:dependencies [[nrepl/nrepl "0.5.3"]
                                          [enlive "1.1.6"]
                                          [amperity/vault-clj "0.6.5"]]
                           :test-paths ^:replace ["integration-test"]
                           :source-paths ^:replace []
                           :resource-paths ^:replace ["integration-resources"]
                           :aot ^:replace []
                           :repl-options {:init-ns user}}

             :integration-vault {:jvm-opts ["-Dconfig_mode=vault"]}}

  :repositories ^:replace [["clojars" {:url "https://clojars.org/repo"}]
                           ["central" {:url "https://repo1.maven.org/maven2/" :snapshots false}]
                           ["releases" {:url "https://sforzando.jfrog.io/sforzando/libs-release-local"
                                        :username [:gpg :env/artifactory_user]
                                        :password [:gpg :env/artifactory_pwd]}]
                           ["plugins" {:url "https://sforzando.jfrog.io/sforzando/plugins-release"
                                       :username [:gpg :env/artifactory_user]
                                       :password [:gpg :env/artifactory_pwd]}]])
