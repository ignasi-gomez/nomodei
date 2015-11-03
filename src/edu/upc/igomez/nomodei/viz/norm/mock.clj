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

(defn mock-generate-wait []
  ;(Thread/sleep db/mock-generate-sleep) 
  (read-line)
  nil)

(defn mock-evolve-norm-basin-N1 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N1"] :description ["Prospective" "Promulgation" "N1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N1"] :description ["Norm" "Violation" "N1"] :time (tc/to-long (time/now)) :type 8999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Sanction" :parameters ["Plant1"] :description ["Sanction" "Plant1"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N1-Abrog []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N1"] :description ["Prospective" "Promulgation" "N1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Heavy Rain" :parameters [""] :description ["Heavy Rain"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Abrogation" :parameters ["N1"] :description ["Abrogation" "N1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "NOT Heavy Rain" :parameters [""] :description ["NOT" "Heavy Rain"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N1"] :description ["Prospective" "Promulgation" "N1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N1"] :description ["Norm" "Violation" "N1"] :time (tc/to-long (time/now)) :type 8999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Sanction" :parameters ["Plant1"] :description ["Sanction" "Plant1"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N2 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "isDay" :parameters ["1"] :description ["isDay" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["1"] :description ["isMonth" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "sampleProvided" :parameters ["Plant1" "20"] :description ["sampleProvided" "(" "Plant1" "20" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["31"] :description ["isDay" "(" "31" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["12"] :description ["isMonth" "(" "12" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Promulgation" :parameters ["N2"] :description ["Retroactive" "Promulgation" "N2"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["1"] :description ["isDay" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["1"] :description ["isMonth" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["31"] :description ["isDay" "(" "31" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["12"] :description ["isMonth" "(" "12" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N2"] :description ["Norm" "Violation" "N2" "sampleProvided" "(" "Plant1" "20" ")"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "visited" :parameters ["Plant1" "Inspector1"] :description ["visited" "(" "Plant1" "Inspector1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "sampleTaken" :parameters ["Inspector1" "Sample1"] :description ["sampleTaken" "(" "Inspector1" "Sample1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Repaired" :parameters ["N2"] :description ["Norm" "Repaired" "N2"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N2-Annul []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "isDay" :parameters ["1"] :description ["isDay" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["1"] :description ["isMonth" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "sampleProvided" :parameters ["Plant1" "20"] :description ["sampleProvided" "(" "Plant1" "20" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["31"] :description ["isDay" "(" "31" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["12"] :description ["isMonth" "(" "12" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Promulgation" :parameters ["N2"] :description ["Retroactive" "Promulgation" "N2"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["1"] :description ["isDay" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["1"] :description ["isMonth" "(" "1" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isDay" :parameters ["31"] :description ["isDay" "(" "31" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isMonth" :parameters ["12"] :description ["isMonth" "(" "12" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N2"] :description ["Norm" "Violation" "N2" "sampleProvided" "(" "Plant1" "20" ")"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "visited" :parameters ["Plant1" "Inspector1"] :description ["visited" "(" "Plant1" "Inspector1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "sampleTaken" :parameters ["Inspector1" "Sample1"] :description ["sampleTaken" "(" "Inspector1" "Sample1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Repaired" :parameters ["N2"] :description ["Norm" "Repaired" "N2"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Annulment" :parameters ["N2"] :description ["Annulment" "N2"] :time (tc/to-long (time/now)) :type 6900 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionCompensated" :parameters ["Plant1"] :description ["SanctionCompensated" "Plant1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N4 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N4"] :description ["Prospective" "Promulgation" "N4"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SignedContract" :parameters ["Plant1" "Industry1"] :description ["SignedContract" "(" "Plant1" "Industry1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "concentrationMet" :parameters ["WaterMass1" "Pollutant1"] :description ["concentrationMet" "(" "WaterMass1" "Pollutant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "discharged" :parameters ["Industry1" "WaterMass1" "Plant1"] :description ["discharged" "(" "Industry1" "WaterMass1" "Plant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N4"] :description ["Norm" "Violation" "N4" "discharged" "inform"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "InformDischarge" :parameters ["Industry1" "WaterMass1" "Plant1"] :description ["InformDischarge" "(" "Industry1" "WaterMass1" "Plant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "concentrationHigh" :parameters ["WaterMass1" "Pollutant1"] :description ["concentrationHigh" "(" "WaterMass1" "Pollutant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "discharged" :parameters ["Industry1" "WaterMass1" "Plant1"] :description ["discharged" "(" "Industry1" "WaterMass1" "Plant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N4"] :description ["Norm" "Violation" "N4" "discharged" "concentrationHigh"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Annulment" :parameters ["N4"] :description ["Annulment" "N4"] :time (tc/to-long (time/now)) :type 6900 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionCompensated" :parameters ["Plant1"] :description ["SanctionCompensated" "Plant1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionCompensated" :parameters ["Plant1"] :description ["SanctionCompensated" "Plant1"] :time (tc/to-long (time/now)) :type 6900 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Promulgation" :parameters ["N4"] :description ["Retroactive" "Promulgation" "N4"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N4"] :description ["Norm" "Violation" "N4" "discharged"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N5 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"] :description ["States(" "GoogleMaps(" "Location" "(" "Plant1" "BunnyPraire" ")))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"] :description ["hasPower(" "GoogleMaps(" "Location" "(" "X" "Y" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "location" :parameters ["Plant1" "=" "BunnyPraire"] :description ["location(" "Plant1" "BunnyPraire)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"] :description ["CountsAs(" "BunnyPraire" "SensitiveArea)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"] :description ["Entailment(" "location(" "W1" "SensitiveArea" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N5"] :description ["Prospective" "Promulgation" "N5"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Sanction" :parameters ["Plant1"] :description ["Sanction" "Plant1"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Abrogation CountsAs" :parameters ["BunnyPraire" "SensitiveArea"] :description ["Abrogation CountsAs(" "BunnyPraire" "SensitiveArea)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)


  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-basin-N5-RetroCount []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"] :description ["States(" "GoogleMaps(" "Location" "(" "Plant1" "BunnyPraire" ")))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"] :description ["hasPower(" "GoogleMaps(" "Location" "(" "X" "Y" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "location" :parameters ["Plant1" "=" "BunnyPraire"] :description ["location(" "Plant1" "BunnyPraire)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"] :description ["CountsAs(" "BunnyPraire" "SensitiveArea)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"] :description ["Entailment(" "location(" "W1" "SensitiveArea" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N5"] :description ["Prospective" "Promulgation" "N5"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionPaid" :parameters ["Plant1"] :description ["SanctionPaid" "Plant1"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Add CountsAs" :parameters ["Treatment1" "StringentTreatment"] :description ["Retroactive Add CountsAs(" "Treatment1" "StringentTreatment)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters ["Treatment1" "StringentTreatment"] :description ["CountsAs" "(" "Treatment1" "StringentTreatment" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionCompensated" :parameters ["Plant1"] :description ["SanctionCompensated" "(" "Plant1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 


(defn mock-evolve-norm-basin-N5-Const []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"] :description ["States(" "GoogleMaps(" "Location" "(" "Plant1" "BunnyPraire" ")))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"] :description ["CountsAs(" "BunnyPraire" "SensitiveArea)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["NhasPower5"] :description ["Prospective" "Promulgation" "hasPower"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"] :description ["hasPower(" "GoogleMaps(" "Location" "(" "X" "Y" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "location" :parameters ["Plant1" "=" "BunnyPraire"] :description ["location(" "Plant1" "BunnyPraire)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"] :description ["Entailment(" "location(" "W1" "SensitiveArea" "))"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Received" :parameters ["Plant1" "WaterMass1"] :description ["Received" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"] :description ["PerformedTreatment" "(" "Plant1" "WaterMass1" "Treatment1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)
  
  (m/insert! :time-line-mock {:event "Discharged" :parameters ["Plant1" "WaterMass1"] :description ["Discharged" "(" "Plant1" "WaterMass1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "SanctionPaid" :parameters ["Plant1"] :description ["SanctionPaid" "Plant1"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-avicena-N1 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Norm Promulgation" :parameters ["N1"] :description ["Norm" "Promulgation" "N5"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasPrescription" :parameters ["Prescription1" "Patient1"] :description ["hasPrescription(" "Prescription1" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isForMedication" :parameters ["Prescription1" "Medication1"] :description ["isForMedication(" "Prescription1" "Medication1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

   (m/insert! :time-line-mock {:event "hasPrescription" :parameters ["Prescription1" "Pharmacist1"] :description ["hasPrescription(" "Prescription1" "Pharmacist1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasDelivered" :parameters ["Pharmacist1" "Medication1" "Patient1"] :description ["hasDelivered(" "Prescription1" "Pharmacist1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N1"] :description ["Norm" "Violation" "N1"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "WarningSent" :parameters ["Competent Authority" "Pharmacist1"] :description ["WarningSent(" "Authority" "Pharmacist1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Abrogation" :parameters ["N1"] :description ["Norm" "Abrogation" "N5"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasPrescription" :parameters ["Prescription1" "Patient1"] :description ["hasPrescription(" "Prescription1" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isForMedication" :parameters ["Prescription1" "Medication1"] :description ["isForMedication(" "Prescription1" "Medication1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "hasDelivered" :parameters ["Pharmacist1" "Medication1" "Patient1"] :description ["hasDelivered(" "Prescription1" "Pharmacist1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-avicena-N5 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "actualTime" :parameters ["initTime"] :description ["actualTime(" "initTime" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "actualTimeLt" :parameters ["finishTime"] :description ["actualTimeLt(" "finishTime" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "exercise" :parameters ["Patient1"] :description ["exercise(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "exercise" :parameters ["Patient1"] :description ["exercise(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Promulgation" :parameters ["N5"] :description ["Retroactive" "Promulgation" "N5"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "exercise" :parameters ["Patient1"] :description ["exercise(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line false :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "increaseReputation" :parameters ["Patient1"] :description ["increaseReputation(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "increaseReputation" :parameters ["Patient1"] :description ["increaseReputation(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "exercise" :parameters ["Patient1"] :description ["exercise(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N5"] :description ["Norm" "Violation" "N5"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "increaseReputation" :parameters ["Patient1"] :description ["increaseReputation(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn mock-evolve-norm-avicena-N6 []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/drop-coll! :time-line-mock)  
  
  (info "Ready to roll!")

  (m/insert! :time-line-mock {:event "Prospective Promulgation" :parameters ["N6"] :description ["Prospective" "Promulgation" "N6"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "isPatient" :parameters ["Patient1"] :description ["isPatient(" "Patient1" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "consume" :parameters ["Patient1" "ECigarrete1"] :description ["consume(" "Patient1" "ECigarrete1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "consume" :parameters ["Patient1" "ECigarrete1"] :description ["consume(" "Patient1" "ECigarrete1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Retroactive Add CountsAs" :parameters ["ECigarrete1" "ToxicSubstance"] :description ["Retroactive Add CountsAs(" "ECigarrete1" "ToxicSubstance)"] :time (tc/to-long (time/now)) :type 6900 :line true :norm false})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters  ["ECigarrete1" "ToxicSubstance"] :description ["CountsAs" "(" "ECigarrete1" "ToxicSubstance" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Entailment" :parameters  ["consume" "Patient1" "ToxicSubstance"] :description ["Entailment" "(" "consume" "Patient1" "ToxicSubstance)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N6"] :description ["Norm" "Violation" "N6"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "logInRecord" :parameters  ["Consume" "Patient1" "ECigarrete1" "Time1"]:description ["logInRecord(" "Consume" "Patient1" "ECigarrete1" "Time1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N6"] :description ["Norm" "Violation" "N6"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "logInRecord" :parameters  ["Consume" "Patient1" "ECigarrete1" "Time1"]:description ["logInRecord(" "Consume" "Patient1" "ECigarrete1" "Time1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "consume" :parameters ["Patient1" "ECigarrete1"] :description ["consume(" "Patient1" "ECigarrete1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "CountsAs" :parameters  ["ECigarrete1" "ToxicSubstance"] :description ["CountsAs" "(" "ECigarrete1" "ToxicSubstance" ")"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Entailment" :parameters  ["consume" "Patient1" "ToxicSubstance"] :description ["Entailment" "(" "consume" "Patient1" "ToxicSubstance)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "logInRecord" :parameters  ["Consume" "Patient1" "ECigarrete1" "Time1"]:description ["logInRecord(" "Consume" "Patient1" "ECigarrete1" "Time1)"] :time (tc/to-long (time/now)) :type 5999 :line true :norm true})
  (info "Mock Insert" @last-time)
  (mock-generate-wait)

  (m/insert! :time-line-mock {:event "Norm Violation" :parameters ["N6"] :description ["Norm" "Violation" "N6"] :time (tc/to-long (time/now)) :type 8999 :line true :norm false})  
  (info "Mock Insert" @last-time)
  (mock-generate-wait)


  (m/drop-coll! :time-line-mock)  

  ))nil) 

(defn -main [& args]
  (let [norm-id (first args)
        norm-id (read-string norm-id)
        _ (Thread/sleep 5000) 
        default "Supported ids: 
                · -6661
                · -66612
                · -6662
                · -66622
                · -6664
                · -6665
                · -66652
                · -66653
                · -7771
                · -7775
                · -7776
                "
        ]
        (case norm-id
        -6661 (mock-evolve-norm-basin-N1)
        -6662 (mock-evolve-norm-basin-N2)
        -66612 (mock-evolve-norm-basin-N1-Abrog)
        -66622 (mock-evolve-norm-basin-N2-Annul)
        -6664 (mock-evolve-norm-basin-N4)
        -6665 (mock-evolve-norm-basin-N5)
        -66652 (mock-evolve-norm-basin-N5-RetroCount)
        -66653 (mock-evolve-norm-basin-N5-Const)
        -7771 (mock-evolve-norm-avicena-N1)
        -7775 (mock-evolve-norm-avicena-N5)
        -7776 (mock-evolve-norm-avicena-N6)
        default)
    ))




