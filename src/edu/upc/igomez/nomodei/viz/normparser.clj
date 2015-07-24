;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2015 Ignasi Gómez Sebastià
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Contributors:
;     Ignasi Gómez-Sebastià - First Tests (2015-07-17) (yyyy-mm-dd)
;                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;lein run -m edu.upc.igomez.nomodei.viz.normparser

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/norm-parser.clj")
;(use 'edu.upc.igomez.nomodei.viz.norm-parser)


(ns edu.upc.igomez.nomodei.viz.normparser
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
        description ["Norm" "Instance" norm norm-instance-id]
        nodeid (keyword norm-instance-id)
        fill (db/select-fill-color type true)
        _ (m/with-mongo db/mongo-conn 
                  (do (m/insert! :norm-instance-graph-node
                        {:norm-instance-id norm-instance-id :x x :y y :description description :nodeid nodeid :ancestor nil :active false :type type :fill fill :timestamp timestamp})))
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
        _ (info "draw-norm-line: " x " " y " " description " " nodeid " " ancestor)
        actual-time (tc/to-long (time/now))
        _ (m/with-mongo db/mongo-conn 
          (do (m/insert! :norm-instance-graph-node
                {:norm-instance-id norm-id :x x :y y :description description :nodeid nodeid :ancestor ancestor :active active :timestamp actual-time})
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

(defn mock-populate-norm-conca-N5 []   
  "Mock-up of norm parsing to demonstrate several scenarios"
  (info "mock-populate-norm-conca-N5")
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -6665}) 
  (m/insert! :norm-instance {:norm-instance-id  -6665
                          :norm "N5"
                          :active [{:event "Active" :parameters []} {:event "Received" :parameters ["Plant1" "WaterMass1"]} {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"]} {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"]}  {:event "location" :parameters ["Plant1" "=" "BunnyPraire"]} {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"]}{:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"]}] 
                          :violated [{:event "Violated" :parameters []} {:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"] :negate true}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}]
                          :fulfilled [{:event "Fulfilled" :parameters []} {:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"]}{:event "CountsAs" :parameters ["Treatment1" "StringentTreatment"]}{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"]}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}] 
                          :repaired [{:event "Repaired" :parameters []} {:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Compensated" :parameters []} {:event "Annulment" :parameters ["N1"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })
  (gen-norm-graph -6665)
  ))nil) 

;lein run -m edu.upc.igomez.nomodei.viz.norm-parser
;db.getCollection("norm-instance").find({"id":-1}).pretty()
;db.getCollection("norm-instance-graph-node").find({"id":-1}).pretty()
(defn mock-populate-norm
  "Wrapper for norm parsing mockup"
  []
  (mock-populate-norm-conca-N5))

(defn -main []
  (let [_ (mock-populate-norm)
        ]
    ))


