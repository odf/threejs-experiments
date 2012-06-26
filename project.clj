; Using Leiningen 1.7.0 or newer:
(defproject lein-cljsbuild-example "1.2.3"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [enfocus "0.9.1-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "0.2.1"]]
  :cljsbuild
  {
   :builds [{:source-path "src-cljs"
             :compiler {:output-to "resources/public/js/main.js"
                        :optimizations :whitespace
                        :externs ["resources/public/js/first_scene.js"]}}]})
