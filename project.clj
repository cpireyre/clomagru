(defproject clomagru "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/test.check "0.10.0"]
                 [ring "1.7.1"]
                 [metosin/ring-http-response "0.9.1"]
                 [ring/ring-defaults "0.3.2"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [seancorfield/next.jdbc "1.0.2"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [crypto-password "0.2.1"]
                 [reagent "0.8.1"]
                 [bouncer "1.0.1"]]
  :repl-options {:init-ns clomagru.core}
  :main ^:skip-aot clomagru.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-dir  "resources/public/goog"
                                   :output-to   "resources/public/app.js"
                                   :pretty-print true
                                   :asset-path  "goog"
                                   :main "clomagru-cljs.core"}}]})
