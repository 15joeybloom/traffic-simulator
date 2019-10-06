(defproject snake "0.0.1"
  :description "A snake game!"
  :dependencies [[org.clojure/clojure "1.9.0" :upgrade false]
                 [org.clojure/clojurescript "1.10.520"]
                 [binaryage/devtools "0.9.10"]
                 [reagent "0.8.1"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-bikeshed "0.5.2"]
            [lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.18"]
            [lein-kibit "0.1.6"]
            [lein-ring "0.12.5"]]

  ;; Compilation
  :aot :all

  ;; Partial setup for interactive development
  :figwheel {:css-dirs ["resources/public/css"]}
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  ;; Compilation/build for CLJS
  :cljsbuild
  {:builds {:dev {:source-paths ["src"]
                  :figwheel {:open-urls ["http://localhost:3449/snake.html"]}
                  :compiler {:main cljs.snake
                             :asset-path "js/compiled/out"
                             :output-to "resources/public/js/compiled/snake.js"
                             :output-dir "resources/public/js/compiled/out"
                             :source-map-timestamp true}}}}
  ;; Profiles - handy for switching between CLJS projects
  :profiles
  {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                        [figwheel-sidecar "0.5.18"]
                        [cider/piggieback "0.4.1"]]
         :source-paths ["src"]
         :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
