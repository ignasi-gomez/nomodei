;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2015 Ignasi Gómez Sebastià
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Code to periodically query updates on events updating norm instance information
; on the visualization DB. 
;
; Contributors:
;     Ignasi Gómez-Sebastià - First Version (2015-07-30) (yyyy-mm-dd)
;                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;lein run -m edu.upc.igomez.nomodei.viz.norm.watcher -6665

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/norm/watcher.clj")
;(use 'edu.upc.igomez.nomodei.viz.norm.watcher)



(ns edu.upc.igomez.nomodei.viz.norm.watcher
   (:use compojure.core
        [clojure.tools.logging :only (info error)]
        lacij.edit.graph
        lacij.edit.dynamic
        lacij.view.graphview
        analemma.xml
        (tikkba swing dom core)
        tikkba.utils.xml
        clojure.set)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.response :as response]
            [clojure.data.json :as json]
            [lacij.layouts.layout :refer :all]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [somnium.congomongo :as m]
            [edu.upc.igomez.nomodei.constants.constants :as db])
  (:import (javax.swing SwingUtilities JFrame JButton BoxLayout JPanel Timer)
           (java.awt.event ActionListener)
           (java.awt Rectangle)
           (org.apache.batik.swing JSVGCanvas JSVGScrollPane)
           (java.awt Component BorderLayout FlowLayout)))

(def last-time (atom (tc/to-long (time/now))))

(defn do-update-node
  "Updates norm from id"
  [id norm-id active]
  (m/with-mongo db/mongo-conn 
  (do
     (let [fill (if (= true active) db/type-active-color db/type-inactive-color)
          _ (info "updating node :" id norm-id active fill)
          _ (m/update! :norm-instance-graph-node
                            {:norm-instance-id norm-id
                            :nodeid id} 
                           {:$set 
                            { :active active
                              :fill fill
                              :timestamp (tc/to-long (time/now))
                              }}
                               :upsert false)
          ]
      ))))
  
(defn update-node
  "Marks a norm node in green as it just happened. Prepares update by generating list of node-ids to be affected"
  [norm norm-id active]
  (info "Marking node: " norm)
  (let [
        node-event (:event norm)
        node-params (:parameters norm)
        negate (:negate norm)
        ;active (if (nil? negate) true (and (not active) negate))
        node-line ["Active" "Violated" "Fulfilled" "Repaired" "Compensated"]
        id (into [] (map #(str % node-event (clojure.string/join "-" node-params)) node-line ))
        _ (doall (map #(do-update-node % norm-id active) id))
  ]nil))
    

(defn update-norm
  "Query database looking for events that might cause norm to change"
  [norm-id]
  (m/with-mongo db/mongo-conn 
  (do
    (let [my-time (deref last-time)
          actual-time (tc/to-long (time/now))
          records  (m/fetch :time-line-mock
                            :where {:time {:$gt my-time}}
                            :only [:event :parameters])
          events (map #(dissoc % :_id) records)
          events (into #{} events)
         _ (info "Events retrieved:" events)
         records  (m/fetch :norm-instance
                            :where {:norm-instance-id norm-id})
          norm (first records)
          norm-events (concat (:active norm) (:violated norm) (:fulfilled norm) (:repaired norm) (:compensated norm))
          norm-events (into #{} norm-events)
          _ (info "Norm events retrieved:" norm-events)
          match (intersection events norm-events)
         _ (info "Match " match)
         _ (doall (map #(update-node %1 norm-id true) match))
         norm-events (into #{} (filter #(contains? %1 :negate)  norm-events))
         _ (info "Norm anti events retrieved:" norm-events)
          match (distinct (map #(remove %1 norm-events) events))
          match (into [] (flatten match))
         _ (info "Anti Match " match)
         _ (doall (map #(update-node %1 norm-id true) match))
         _ (reset! last-time actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (update-norm norm-id))
  nil)

(defn -main [& args]
  (let [norm-id (first args)
        norm-id (read-string norm-id)
        fut (future (update-norm norm-id))
        ]
    ))


