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

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/constants/constants.clj")
;(use 'edu.upc.igomez.nomodei.constants.constants)

(ns edu.upc.igomez.nomodei.constants.constants
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

(def norm-incx
  (:norm.incx props))

(def norm-incy
  (:norm.incy props))

(def query-sleep
  (:query.sleep props))

(def type-head-name
  (:type.head.name props))

(def type-head-color
  (:type.head.color props))

(def type-sub-head-color
  (:type.subhead.color props))

(def type-active-color
  (:type.active.color props))

(def type-inactive-color
  (:type.inactive.color props))

(def sub-heads
  #{"Active" 
  "Violated" 
  "Fulfilled"
  "Repaired"
  "Compensated"})

(defn select-fill-color
  "Chooses node fill color based on the node's type and status"
  [node-type node-status]
  (let
    [status-color (if node-status type-active-color type-inactive-color)
     node-type-color (if (= node-type type-head-name) type-head-color (if (contains? sub-heads node-type) type-sub-head-color status-color))
    color node-type-color
    ]
    color)) 

(def norm-head-x
  (:norm.head.posx props))

(def norm-head-y
  (:norm.head.posy props))

(def norm-node-height
  (:norm.node.height props))

(def norm-node-width
  (:norm.node.width props))

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
