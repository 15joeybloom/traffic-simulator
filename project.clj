(defproject myapp "0.0.1"
  :description "A demo app showing how CLJS works:: Full output mode"
  :dependencies [[cheshire "5.8.1"]
                 [clj-http "3.9.1"]
                 [coreagile/defenv "1.0.2"]
                 [hiccup "1.0.5"]
                 [http-kit "2.3.0"]
                 [metosin/compojure-api "1.1.12"]
                 [org.clojure/clojure "1.9.0" :upgrade false]
                 [org.clojure/core.async "0.3.442"
                  :exclusions [org.clojure/tools.reader]]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]


                 [cljs-http "0.1.46"]
                 [cljs-ajax "0.7.5"]
                 [cljsjs/react "16.8.6-0"]
                 [cljsjs/react-dom "16.8.6-0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [binaryage/devtools "0.9.10"]
                 [clj-commons/secretary "1.2.4"]
                 [day8.re-frame/tracing "0.5.1"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [ring "1.7.1"]
                 [sablono "0.7.4"]
                 ]

  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7"]
            
            [lein-ancient "0.6.15"]
            [lein-bikeshed "0.5.2"]
            [lein-kibit "0.1.6"]
            [lein-ring "0.12.5"]]

  ;; Used to make this compatible with Java 11
  :managed-dependencies [[org.clojure/core.rrb-vector "0.0.13"]
                         [org.flatland/ordered "1.5.7"]]

  ;; Server setup
  :ring {:handler clj.handler/app
         :init clj.handler/init-routes}

  ;; Compilation
  :aot :all
  :uberjar-name "myapp.jar"
  :main clj.handler

  ;; Partial setup for interactive development
  :figwheel {:ring-handler clj.handler/app
             :css-dirs ["resources/public/css"]}
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  ;; Compilation/build for CLJS
  :cljsbuild
  {:builds
   {
    :dev {:source-paths ["src/cljs"]
          :figwheel {:open-urls ["http://localhost:3449/example"]}
          :compiler {:main myapp.example
                     :asset-path "js/compiled/out"
                     :output-to "resources/public/js/compiled/example.js"
                     :output-dir "resources/public/js/compiled/out"
                     :source-map-timestamp true
                     :npm-deps {:capitalize "2.0.0"}
                     :install-deps true}}

    :reframe {:source-paths ["src/cljs_reframe"]
              :figwheel {:open-urls ["http://localhost:3449/reframe"]
                         :on-jsload "cljs_reframe.core/main"}
              :compiler {:main cljs_reframe.core
                         :asset-path "js/compiled/out_cljs_reframe"
                         :output-to
                         "resources/public/js/compiled/cljs_reframe.js"
                         :output-dir
                         "resources/public/js/compiled/out_cljs_reframe"
                         :source-map true
                         :source-map-timestamp true}}}

   }

  ;; Profiles - handy for switching between CLJS projects
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.18"]
                                  [cider/piggieback "0.4.1"]]
                   :source-paths ["src/cljs"]
                   :repl-options {:nrepl-middleware
                                  [cider.piggieback/wrap-cljs-repl]}}

              :reframe {:dependencies [[binaryage/devtools "0.9.10"]
                                                        [figwheel-sidecar "0.5.18"]
                                                        [cider/piggieback "0.4.1"]]
                                         :source-paths ["src/cljs_reframe"]
                                         :repl-options {:nrepl-middleware
                                                        [cider.piggieback/wrap-cljs-repl]}}})
