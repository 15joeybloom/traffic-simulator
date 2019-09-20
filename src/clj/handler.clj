(ns clj.handler
  (:gen-class)
  (:require [clj.env :as env]
            [compojure.api.sweet :refer :all]
            [org.httpkit.server :as httpkit]
            [ring.util.http-response :refer :all]
            [ring.middleware.resource :refer [wrap-resource]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def mock-data
  [{:name "Bank of America",
    :error_institution 0.01,
    :error_plaid 0,
    :success 0.99,
    :last_status_change "2019-05-14T00:00:48Z",
    :status "HEALTHY"}
   {:name "Chase",
    :error_institution 0,
    :error_plaid 0,
    :success 1,
    :last_status_change "2019-05-14T00:00:48Z",
    :status "CRITICAL"}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialization message
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn init-message []
  (println "Hello! Starting service on port..." (@env/env :port)))

(def app
  (wrap-resource
   (api
    {:swagger
     {:ui "/swagger"
      :spec "/swagger.json"
      :data {:info {:title "Example clojure web server"
                    :description "Part of a demo for a full-stack app"}
             :tags [{:name "api", :description "some apis"}]}}}

    (GET "/" []
      (-> (resource-response "index.html" {:root "public"})
          (content-type "text/html")))

    (GET "/example" []
      (-> (resource-response "example.html" {:root "public"})
          (content-type "text/html")))

    (GET "/reframe" []
      (-> (resource-response "reframe.html" {:root "public"})
          (content-type "text/html")))

    (GET "/ping" []
      (ok {:service-status "ok"}))

    (GET "/mock-data" []
      (ok mock-data))

    ) "public"))

(defn -main [& _]
  (init-message)
  (httpkit/run-server #'app {:port (@env/env :port)}))

(comment
  (ok {:service-status "ok"})
  (-main)
  )
