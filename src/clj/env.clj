(ns clj.env
  (:require  [defenv.core :as env :refer [env->map
                                          parse-boolean
                                          parse-int]]))

(def env
  (delay
   (let [spec
         {:port
          {:env-name "FULL_STACK_TEMPLATE_PORT"
           :tfn parse-int
           :default "5000"
           :doc "Port to listen on"}

          :live-db?
          {:env-name "FULL_STACK_TEMPLATE_LIVE_DB"
           :default "false"
           :tfn parse-boolean
           :doc "Postgres Database - Live DB on RDS? If so, need SSL"}}
         env (env->map spec)]
     (env/display-env spec)
     env)))
