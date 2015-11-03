;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2015 Ignasi Gómez Sebastià
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Code to parse norm instances inserting their basic information on the
; visualization DB. 
; Includes code to mock-up norm parsing
;
; Contributors:
;     Ignasi Gómez-Sebastià - First Version (2015-07-17) (yyyy-mm-dd)
;                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;lein run -m edu.upc.igomez.nomodei.viz.norm.parser

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/norm/parser.clj")
;(use 'edu.upc.igomez.nomodei.viz.norm.parser)


(ns edu.upc.igomez.nomodei.viz.norm.parser
   (:use 
            [clojure.tools.logging :only (info error)])
  (:require 
            [somnium.congomongo :as m]
            [edu.upc.igomez.nomodei.constants.constants :as db]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]))

(def last-x (atom 10))
(def last-y (atom 120))
(def last-time (atom 0))
(def last-node (atom (keyword "Norm")))

(defn gen-norm-graph-head 
  "Stores norm graph node for initial norm graph with the name of the norm and the id of the instance"
  [norm-instance-id norm]
  (let [timestamp (tc/to-long (time/now))
        type db/type-head-name
        x db/norm-head-x
        y db/norm-head-y
        height db/norm-node-height
        width db/norm-node-width
        description ["Norm" "Instance" (str norm " "norm-instance-id)]
        nodeid (str "Norm" norm-instance-id)
        fill (db/select-fill-color type true)
        _ (m/with-mongo db/mongo-conn 
                  (do (m/insert! :norm-instance-graph-node
                        {:norm-instance-id norm-instance-id :x x :y y :description description :nodeid nodeid :ancestor nil :active false :type type :height height :width width :fill fill :timestamp timestamp})))
                    ]nil) nil)



(defn gen-norm-node
  [par-event par-line norm-id]
   "Stores a norm graph node"
  (let [x (deref last-x)
        y (deref last-y)
        ancestor (deref last-node)
        node-event (:event par-event)
        node-params (:parameters par-event)
        node-line par-line
        id (str node-line node-event (clojure.string/join "-" node-params))
        nodeid (keyword id)
        negated (get par-event :negate false)
        node-event (if negated (clojure.string/join "" ["NOT " node-event]) node-event)
        description (into [node-event] node-params)
        inc_x db/norm-incx
        inc_y db/norm-incy
        active false
        type node-event
        fill (db/select-fill-color type active)
        height db/norm-node-height
        width db/norm-node-width
        _ (info "draw-norm-line: " x " " y " " description " " nodeid " " ancestor)
        actual-time (tc/to-long (time/now))
        _ (m/with-mongo db/mongo-conn 
          (do (m/insert! :norm-instance-graph-node
                {:norm-instance-id norm-id :x x :y y :description description :nodeid nodeid :ancestor ancestor :active active :timestamp actual-time :fill fill :height height :width width})
            ))
  _ (reset! last-x (+ inc_x x))
   _ (reset! last-y (+ inc_y y))
  _ (reset! last-node nodeid)
  ]
  nil)  nil)

(defn gen-norm-graph 
  [norm-instance-id]
  "Generates norm instance graph based on norm instance definition"
  (m/with-mongo db/mongo-conn 
  (do
    (let [records  (m/fetch :norm-instance
                            :where {:norm-instance-id norm-instance-id})
          _ (m/destroy! :norm-instance-graph-node {:norm-instance-id norm-instance-id}) 
          records (first records)
          norm (:norm records)
          _ (info "gen-norm-graph" records)
          _ (gen-norm-graph-head norm-instance-id norm)

          active (:active records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 10)
          _ (doall (map #(gen-norm-node % "Active" norm-instance-id) active))

          violated (:violated records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 210)
          _ (doall (map #(gen-norm-node % "Violated" norm-instance-id) violated))

          fulfilled (:fulfilled records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 410)
          _ (doall (map #(gen-norm-node % "Fulfilled" norm-instance-id) fulfilled))

          repaired (:repaired records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 610)
          _ (doall (map #(gen-norm-node % "Repaired" norm-instance-id) repaired))

          compensated (:compensated records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 810)
          _ (doall (map #(gen-norm-node % "Compensated" norm-instance-id) compensated))
         ]nil))) 
  nil)

(defn mock-populate-norm-conca-N1 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-conca-N1")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -6661}) 
  (m/insert! :norm-instance {:norm-instance-id  -6661
                          :norm "Basin-N1"
                          :active [{:event "Active" :parameters []} {:event "Received" :parameters ["Plant1" "WaterMass1"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "SecondaryTreatment"] :negate true}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"]}{:event "CountsAs" :parameters ["Treatment1" "SecondaryTreatment"]}{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "SecondaryTreatment"]}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}] 
                          :repaired [{:event "Repaired" :parameters []} {:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "Annulment" :parameters ["N1"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })
  (gen-norm-graph -6661)
  ))nil) 

(defn mock-populate-norm-conca-N2 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-conca-N2")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -6662}) 
  (m/insert! :norm-instance {:norm-instance-id  -6662
                          :norm "Basin-N2"
                          :active [{:event "Active" :parameters []} {:event "isDay" :parameters ["1"]} {:event "isMonth" :parameters ["1"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "isDay" :parameters ["31"]} {:event "isMonth" :parameters ["12"]} {:event "sampleProvided" :parameters ["Plant1" "24"] :negate true}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "sampleProvided " :parameters ["Plant1" "24"]}] 
                          :repaired [{:event "Repaired" :parameters []} {:event "visited" :parameters ["Plant1" "Inspector1"]} {:event "sampleTaken" :parameters ["Inspector1" "Sample1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "Annulment" :parameters ["N2"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })
  (gen-norm-graph -6662)
  ))nil) 

(defn mock-populate-norm-conca-N4 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-conca-N4")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -6664}) 
  (m/insert! :norm-instance {:norm-instance-id  -6664
                          :norm "Basin-N4"
                          :active [{:event "Active" :parameters []} {:event "SignedContract" :parameters ["Plant1" "Industry1"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "discharged" :parameters ["Industry1" "WaterMass1" "Plant1"]} {:event "concentrationHigh" :parameters ["WaterMass1" "Pollutant1"]} {:event "InformDischarge" :parameters ["Industry1" "WaterMass1" "Plant1"] :negate true}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "discharged" :parameters ["Industry1" "WaterMass1" "Plant1"]} {:event "concentrationMet" :parameters ["WaterMass1" "Pollutant1"]} {:event "InformDischarge " :parameters ["Industry1" "WaterMass1" "Plant1"]}]
                          :repaired [{:event "Repaired" :parameters []} {:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "Annulment" :parameters ["N4"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })
  (gen-norm-graph -6664)
  ))nil) 

(defn mock-populate-norm-conca-N5 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-conca-N5")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -6665}) 
  (m/insert! :norm-instance {:norm-instance-id  -6665
                          :norm "Basin-N5"
                          :active [{:event "Active" :parameters []} {:event "Received" :parameters ["Plant1" "WaterMass1"]} {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"]} {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"]}  {:event "location" :parameters ["Plant1" "=" "BunnyPraire"]} {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"]}{:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"] :negate true}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"]}{:event "CountsAs" :parameters ["Treatment1" "StringentTreatment"]}{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"]}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}] 
                          :repaired [{:event "Repaired" :parameters []} {:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })
  (gen-norm-graph -6665)
  ))nil) 

(defn mock-populate-norm-avicena-N1 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-avicena-N1")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -7771}) 
  (m/insert! :norm-instance {:norm-instance-id  -7771
                          :norm "Avicena-N1"
                          :active [{:event "Active" :parameters []} {:event "hasPrescription" :parameters ["Prescription1" "Patient1"]} {:event "isForMedication" :parameters ["Prescription1" "Medication1"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "hasDelivered" :parameters ["Pharmacist1" "Medication1" "Patient1"]} {:event "hasPrescription" :parameters ["Prescription1" "Patient1"]} {:event "identified" :parameters ["Pharmacist1" "Patient1"] :negate true}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "hasDelivered" :parameters ["Pharmacist1" "Medication1" "Patient1"]} {:event "hasPrescription" :parameters ["Prescription1" "Pharmacist1"]} {:event "identified " :parameters ["Pharmacist1" "Patient1"]}]
                          :repaired [{:event "Repaired" :parameters []} {:event "WarningSent" :parameters ["Competent Authority" "Pharmacist1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "SanctionCompensated" :parameters ["Pharmacist1"]}] 
                          })
  (gen-norm-graph -7771)
  ))nil) 

(defn mock-populate-norm-avicena-N5 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-avicena-N5")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -7775}) 
  (m/insert! :norm-instance {:norm-instance-id  -7775
                          :norm "Avicena-N5"
                          :active [{:event "Active" :parameters []} {:event "actualTime" :parameters ["initTime"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "actualTimeLt" :parameters ["finishTime"]} {:event "exercise" :parameters ["Patient1"]}] 
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "actualTimeGt" :parameters ["finishTime"]} {:event "exercise " :parameters ["Patient1"] :negate true}]
                          :repaired [{:event "Repaired" :parameters []} {:event "increaseReputation" :parameters ["Patient1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "SanctionCompensated" :parameters ["Patient1"]}] 
                          })
  (gen-norm-graph -7775)
  ))nil) 

(defn mock-populate-norm-avicena-N6 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-avicena-N6")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -7776}) 
  (m/insert! :norm-instance {:norm-instance-id  -7776
                          :norm "Avicena-N6"
                          :active [{:event "Active" :parameters []} {:event "isPatient" :parameters ["Patient1"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "consume" :parameters ["Patient1" "ECigarrete1"]} {:event "CountsAs" :parameters ["ECigarrete1" "ToxicSubstance"]}{:event "Entailment" :parameters ["consume" "Patient1" "ToxicSubstance"]}] 
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "False" :parameters [""]}]
                          :repaired [{:event "Repaired" :parameters []} {:event "logInRecord" :parameters ["Consume" "Patient1" "ECigarrete1" "Time1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "SanctionCompensated" :parameters ["Patient1"]}] 
                          })
  (gen-norm-graph -7776)
  ))nil) 


(defn populate-norms
  "Parse norms and generate basic visualization graphs"
  []
  (mock-populate-norm-conca-N1)
  (mock-populate-norm-conca-N2)
  (mock-populate-norm-conca-N4)
  (mock-populate-norm-conca-N5)
  (mock-populate-norm-avicena-N1)
  (mock-populate-norm-avicena-N5)
  (mock-populate-norm-avicena-N6)
  )

;lein run -m edu.upc.igomez.nomodei.viz.norm.parser
;db.getCollection("norm-instance").find({"norm-instance-id":-6665}).pretty()
;db.getCollection("norm-instance-graph-node").find({"norm-instance-id":-6665}).pretty()
(defn -main []
  (let [_ (populate-norms)
        ]
    ))


