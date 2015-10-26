(defproject brutha "0.2.0"
  :description "Simple ClojureScript interface to React"
  :url "https://github.com/weavejester/brutha"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145" :scope "provided"]
                 [cljsjs/react-dom "0.14.0-0"]]
  :plugins [[lein-cljsbuild "1.1.0"]]
  :cljsbuild
  {:test-commands {"unit-tests" ["phantomjs" :runner "target/main.js"]}
   :builds {:main {:source-paths ["src"], :compiler {:output-to "target/main.js"}}}}
  :profiles
  {:dev {:plugins [[com.cemerick/clojurescript.test "0.3.3"]]
         :cljsbuild {:builds {:main {:source-paths ["test"]}}}}})
