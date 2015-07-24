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

;lein run -m edu.upc.igomez.nomodei.viz.mockups.norm

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/mockups/norm.clj")
;(use 'edu.upc.igomez.nomodei.viz.mockups.norm)


(ns edu.upc.igomez.nomodei.viz.mockups.norm
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
            [edu.upc.igomez.nomodei.db.db :as db])
  (:import (javax.swing SwingUtilities JFrame JButton BoxLayout JPanel Timer)
           (java.awt.event ActionListener)
           (java.awt Rectangle)
           (org.apache.batik.swing JSVGCanvas JSVGScrollPane)
           (java.awt Component BorderLayout FlowLayout)))

;db.getCollection("norm-instance").find({"id":-1}).pretty()
(defn mock-populate-norm []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-instance {:norm-instance-id -1}) 
  (m/insert! :norm-instance {:norm-instance-id  -1
                          :norm "N1"
                          :active [{:event "Active" :parameters []} {:event "Received" :parameters ["Plant1" "WaterMass1"]} {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"]} {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"]}  {:event "location" :parameters ["Plant1" "=" "BunnyPraire"]} {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"]}{:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"]}] 
                          :violated [{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"] :negate true}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}]
                          :fulfilled [{:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"]}{:event "CountsAs" :parameters ["Treatment1" "StringentTreatment"]}{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"]}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}] 
                          :repaired [{:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Annulment" :parameters ["N1"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })

  ))nil) 

(defn gen-graph
  "Draws initial graph with the node Monitor Start"
  []
  (-> (graph)
       (add-node (keyword "Norm") ["Norm"]  :x 10 :y 400 :height 100 :style {:fill "lavender" :stroke "red"})))

(defn create-frame
  "Creation of SWING component to view graph"
  [svgcanvas]
  (let [actionspanel (JPanel. )
        frame (JFrame.)
        pane (.getContentPane frame)]
    (.setLayout actionspanel (FlowLayout.))
    (.add pane actionspanel BorderLayout/PAGE_START)
    (.add pane svgcanvas BorderLayout/CENTER)
    (.setSize frame 1700 1300)
    (.setSize svgcanvas 1700 1300)
    frame))

(def ^{:dynamic true} *graph* (atom (gen-graph)))

(def ^{:dynamic true} pane (atom ""))

(def last-x (atom 10))
(def last-time (atom 0))
(def last-time-viz (atom 0))
(def last-y (atom 120))
(def last-node (atom (keyword "Norm")))

(defn draw-norm-line
  [par-event par-line norm-id]
  "Draw an event on the time-line"
  (let [g (deref *graph*)
        svgcanvas (:svgcanvas g)
        x (deref last-x)
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
        inc_x 200
        inc_y 0
        fill "burlywood"
        _ (info "Drawing: " x " " y " " description " " nodeid " " ancestor)
        actual-time (tc/to-long (time/now))
        _ (m/with-mongo db/mongo-conn 
          (do (m/insert! :norm-instance-graph-node
                {:norm-instance-id norm-id :x x :y y :description description :nodeid nodeid :ancestor ancestor :active false :timestamp actual-time})
            ))
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid description :x x :y y :height 100 :width 150 :style {:fill fill :stroke "red"})
            g (add-edge! g (keyword (gensym "edge")) ancestor nodeid)
            g (set-node-selected! g nodeid true)]
        (reset! *graph* g)))
  _ (reset! last-x (+ inc_x x))
   _ (reset! last-y (+ inc_y y))
  _ (reset! last-node nodeid)
  ]
  nil)
  (Thread/sleep db/draw-sleep) 
  nil)

(defn query-norm-time-line
  [norm-id]
  "Query mongoDB looking for events inserted after last query. Orders events by time and type. 
  See collection time-line-type for types. Print events on graph"
  (Thread/sleep 20000) 
  (m/with-mongo db/mongo-conn 
  (do
    (let [records  (m/fetch :norm-instance
                            :where {:id norm-id})
          _ (m/destroy! ::norm-instance-graph-node {:norm-instance-id norm-id}) 
          records (first records)
          norm (:norm records)
          _ (info records)

          active (:active records)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 10)
          _ (doall (map #(draw-norm-line % "Active" norm-id) active))

          violated (:violated records)
          violated (concat [{:event "Violated" :parameters []}] violated)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 210)
          _ (doall (map #(draw-norm-line % "Violated" norm-id) violated))

          fulfilled (:fulfilled records)
          fulfilled (concat [{:event "Fulfilled" :parameters []}] fulfilled)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 410)
          _ (doall (map #(draw-norm-line % "Fulfilled" norm-id) fulfilled))

          repaired (:repaired records)
          repaired (concat [{:event "Repaired" :parameters []}] repaired)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 610)
          _ (doall (map #(draw-norm-line % "Repaired" norm-id) repaired))

          compensated (:compensated records)
          compensated (concat [{:event "Compensated" :parameters []}] compensated)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 810)
          _ (doall (map #(draw-norm-line % "Compensated" norm-id) compensated))
         ]nil))) 
  nil)

(defn do-update-node
  "Updates norm from id"
  [id norm-id]
  (info "updating node :" id norm-id)
  (m/with-mongo db/mongo-conn 
  (do
    (m/update! :norm-instance-graph-node 
                            {:norm-instance-id norm-id
                            :nodeid id} 
                           {:$set 
                            { :active true
                              :timestamp (tc/to-long (time/now))
                              }}
                               :upsert false))))
  
(defn update-node
  "Marks a norm node in green as it just happened. Prepares update by generating list of node-ids to be affected"
  [norm norm-id]
  (info "Marking node: " norm)
  (let [
        node-event (:event norm)
        node-params (:parameters norm)
        node-line ["Active" "Violated" "Fulfilled" "Repaired" "Compensated"]
        id (into [] (map #(str % node-event (clojure.string/join "-" node-params)) node-line ))
        _ (info "id " id)
        _ (doall (map #(do-update-node % norm-id) id))
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
                            :where {:id norm-id})
          norm (first records)
          norm-events (concat (:active norm) (:violated norm) (:fulfilled norm) (:repaired norm) (:compensated norm))
          norm-events (into #{} norm-events)
          _ (info "Norm events retrieved:" norm-events)
          match (intersection events norm-events)
         _ (info "Match " match)
         _ (doall (map #(update-node %1 norm-id) match))
         norm-events (into #{} (filter #(contains? %1 :negate)  norm-events))
         _ (info "Norm anti events retrieved:" norm-events)
          match (distinct (map #(remove %1 norm-events) events))
          match (into [] (flatten match))
         _ (info "Anti Match " match)
         _ (doall (map #(update-node %1 norm-id) match))
         _ (reset! last-time actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (update-norm norm-id))
  nil)


(defn mock-evolve-norm []   
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

(defn update-norm-viz-node
  [par-node norm-id]
  "Redraws a particular node in the norm visualization as it has been updated"
  (let [g (deref *graph*)
        active (:active par-node)
        fill (if active "mediumaquamarine" "burlywood")
        nodeid (:nodeid par-node)
        description (:description par-node)
        x (:x par-node)
        y (:y par-node)
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid description :x x :y y :height 100 :width 150 :style {:fill fill :stroke "red"})]
        (reset! *graph* g)))
  ]
  nil) nil)

(defn update-norm-viz
  "Query database looking for events that might cause norm to change"
  [norm-id]
  (m/with-mongo db/mongo-conn 
  (do
    (let [my-time (deref last-time-viz)
          actual-time (tc/to-long (time/now))
          records  (m/fetch :norm-instance-graph-node
                            :where {:timestamp {:$gt my-time}})
         _ (info "Nodes retrieved:" records)
         _ (doall (map #(update-norm-viz-node %1 norm-id) records))
         _ (reset! last-time-viz actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (update-norm-viz norm-id))
  nil)

(defn -main []
  (let [_ (mock-populate-norm)
        g (deref *graph*)
        doc (:xmldoc g)
        svgcanvas (:svgcanvas g)
        jsvgscrollpane (jsvgscrollpane svgcanvas)
        _ (reset! pane jsvgscrollpane)
        updatemanager (update-manager svgcanvas)
        frame (create-frame jsvgscrollpane)
        g (build g)
        _ (reset! *graph* g)
        fut (future (query-norm-time-line -1))
        _ (SwingUtilities/invokeAndWait
          (fn [] (.setVisible frame true)))
        _ (Thread/sleep 40000) 
        fut (future (mock-evolve-norm))
        fut (future (update-norm -1))
        fut (future (update-norm-viz -1))
        ]
    ))


