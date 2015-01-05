(defproject tantan "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "target/generated/clj" "target/generated/cljx"]

  :test-paths ["spec/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2496" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [ring-transit "0.1.3"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [om "0.8.0-beta3"]
                 [environ "1.0.0"]
                 [http-kit "2.1.19"]
                 [sablono "0.2.22"]
                 [cljs-http "0.1.24"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "tantan.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "target/generated/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :env { :analects-data-dir "/home/arne/github/analects-data" }

  :profiles {:dev {:source-paths ["env/dev/clj"]

                   :dependencies [[figwheel "0.1.6-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.1.3"]
                                  [weasel "0.4.2"]
                                  [leiningen "2.5.0"]
                                  [speclj "3.1.0"]]

                   :repl-options {:init-ns tantan.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]}

                   :plugins [[lein-figwheel "0.1.6-SNAPSHOT"]
                             [speclj "3.1.0"]
                             [com.keminglabs/cljx "0.5.0" :exclusions [org.clojure/clojure]]]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :cljsbuild {:builds
                               {:app
                                {:source-paths ["env/dev/cljs"]}

                                 :dev {:source-paths ["src/cljs"  "spec/cljs"]
                                       :compiler {:output-to     "resources/public/js/app_spec.js"
                                                  :output-dir    "resources/public/js/spec"
                                                  :source-map    "resources/public/js/spec.js.map"
                                                  :preamble      ["react/react.min.js"]
                                                  :externs       ["react/externs/react.js"]
                                                  :optimizations :whitespace
                                                  :pretty-print  false}
                                       :notify-command ["phantomjs"  "bin/speclj" "resources/public/js/app_spec.js"]}}}


                   :test-commands {"spec" ["phantomjs" "bin/speclj" "resources/public/js/app_spec.js"]}

                   :prep-tasks [["cljx" "once"] "javac" "compile"]

                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/generated/clj"
                                    :rules :clj}
                                   {:source-paths ["src/cljx"]
                                    :output-path "target/generated/cljs"
                                    :rules :cljs}]}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [leiningen.cljsbuild cljx.hooks]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
