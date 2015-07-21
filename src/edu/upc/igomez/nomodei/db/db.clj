;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2015 Ignasi Gómez Sebastià
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Contributors:
;     Ignasi Gómez-Sebastià - Wrapper to mongoDB (2015-07-17) (yyyy-mm-dd)
;                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(ns edu.upc.igomez.nomodei.db.db
 (:use [clojure.tools.logging :only (info error)])
  (:require [snippets-generic :as cs]
             [somnium.congomongo :as m]
             [clojure.string :as str]))

(def props
  "Properties taken from `aggregator.properties`. These include:

  * Information (e.g., name, coordinates) about the city to crawl.
  * Access configuration to access the `mongo` instance."
  (cs/load-props "nomodei.properties"))

(def mongo-uri
  "The URI identifying the `mongo` connection. It is used by other
   namespaces, such as `atalaya.api.atalaya-api`."
  (str "mongodb://" (:mongo.user props) ":" (:mongo.pass props) "@"
       (:mongo.host props) ":" (:mongo.port props) "/" (:mongo.db props)))

(def mongo-conn
  "Object with the permanent open connection with the `mongo` instance."
  (try
     (print mongo-uri)
    (m/make-connection mongo-uri)
    (catch Exception e
      (print mongo-uri))))

(def query-sleep
  (:query.sleep props))

(def draw-sleep
  (:draw.sleep props))

(def mock-generate-sleep
  (:mock-generate.sleep props))

(def font-size
  (str (:graph.font-size props)))

;To help identify directory for properties files and content
(defn db-info
  []
  (str "MongoDB Config params" ["DB:" (:mongo.db props) 
                                 "USER:" (:mongo.user props)
                                 "PASS:" (:mongo.pass props)
                                 "HOST:" (:mongo.host props)
                                 "PORT:" (:mongo.port props)]
        "And the current dir is..." (System/getProperty "user.dir")))
