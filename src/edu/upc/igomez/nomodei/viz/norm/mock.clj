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

;lein run -m edu.upc.igomez.nomodei.viz.norm.mock

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/norm/mock.clj")
;(use 'edu.upc.igomez.nomodei.viz.norm.mock)


(ns edu.upc.igomez.nomodei.viz.norm.mock
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

(defn mock-evolve-norm-N5 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999})
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass2"] :description ["Received" "(" "Plant1" "WaterMass2" ")"] :time (tc/to-long (time/now)) :type 5999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass2"] :description ["Discharged" "(" "Plant1" "WaterMass2" ")"] :time (tc/to-long (time/now)) :type 5999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N1"] :description ["Prospective" "Promulgation" "N1"] :time (tc/to-long (time/now)) :type 6900})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass2"] :description ["Received" "(" "Plant1" "WaterMass3" ")"] :time (tc/to-long (time/now)) :type 5999})
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass3"] :description ["Discharged" "(" "Plant1" "WaterMass3" ")"] :time (tc/to-long (time/now)) :type 5999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N1"] :description ["Norm" "Violation" "N1"] :time (tc/to-long (time/now)) :type 8999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 

  (m/insert! :time-line-mock {:event "Sanction" :parameters ["Plant1"] :description ["Sanction" "Plant1"] :time (tc/to-long (time/now)) :type 5999})  
  (info "Mock Insert" @last-time)
  (Thread/sleep db/mock-generate-sleep) 
  ))nil) 



(defn -main []
  (let [_ (Thread/sleep 5000) 
        _ (mock-evolve-norm-N5)
        ]
    ))


