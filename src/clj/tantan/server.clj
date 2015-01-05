(ns tantan.server
  (:require [clojure.java.io :as io]
            [tantan.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [tantan.api :as api]
            [compojure.core :refer [GET defroutes routes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [ring.util.response :as response]))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))

(defroutes my-routes
  (resources "/")
  (resources "/react" {:root "react"})
  (wrap-transit-response
   (GET "/entry/:entry" [entry]
        (api/find-entry entry)))
  (GET "/*" req
       (page)))

(def http-handler
  (let [handler (-> #'my-routes
                    (wrap-defaults api-defaults))]
    (if is-dev?
      (reload/wrap-reload handler)
      handler)))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-server http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel))

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
