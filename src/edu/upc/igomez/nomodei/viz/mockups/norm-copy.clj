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
        tikkba.utils.xml)
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

;(defn gen-graph
;  "Draws initial graph with the node Monitor Start"
;  []
;  (-> (graph)
;      (add-node (keyword "Norm") "Norm" :x 500 :y 10 :height 100 :style {:fill "azure" :stroke "red"})))

(defn gen-graph
  "Draws initial graph with the node Monitor Start"
  []
  (-> (graph)
      (add-node (keyword "Monitor-Start") ["Monitor" "Start"]  :x 10 :y 10 :height 100 :style {:fill "lavender" :stroke "red"})))

(def ^{:dynamic true} *graph* (atom (gen-graph)))
(def ^{:dynamic true} pane (atom ""))

(def last-x (atom 20))
(def last-time (atom 0))
(def last-y (atom 270))
(def last-node (atom (keyword "Monitor-Start")))

;db.getCollection("norm-state").find({"id":-1}).pretty()
(defn mock-populate-norm []   
  "Mock-up of event generation to demonstrate several scenarios"
  (m/with-mongo db/mongo-conn 
  (do
  (m/destroy! :norm-state {:id -1}) 
  (m/insert! :norm-state {:id -1
                          :norm "N1"
                          :active [{:event "Received" :parameters ["Plant1" "WaterMass1"]} {:event "Statement" :parameters ["GoogleMaps" "Location" "Plant1" "=" "BunnyPraire"]} {:event "hasPower" :parameters ["GoogleMaps" "Location" "X" "=" "Y"]}  {:event "location" :parameters ["Plant1" "=" "BunnyPraire"]} {:event "CountsAs" :parameters ["BunnyPraire" "SensitiveArea"]}{:event "Entailment" :parameters ["Location" "W1" "SensitiveArea"]}] 
                          :violated [{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"] :negate true}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}]
                          :fulfilled [{:event "PerformedTreatment" :parameters ["Plant1" "WaterMass1" "Treatment1"]}{:event "CountsAs" :parameters ["Treatment1" "StringentTreatment"]}{:event "Entailment" :parameters ["PerformedTreatment" "Plant1" "WaterMass1" "StringentTreatment"]}{:event "Discharged" :parameters ["Plant1" "WaterMass1"]}] 
                          :repaired [{:event "SanctionPaid" :parameters ["Plant1"]}] 
                          :compensated [{:event "Annulment" :parameters ["N1"]}{:event "SanctionCompensated" :parameters ["Plant1"]}] 
                          })

  ))nil) 

(defn draw-node
  [par-event par-line]
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
        description (into [node-event] node-params)
        inc_x 0
        inc_y 160
        fill "burlywood"
        _ (info "Drawing: " x " " y " " description " " nodeid " " ancestor)
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid description :x x :y y :height 100 :style {:fill fill :stroke "red"})
            g (add-edge! g (keyword (gensym "edge")) ancestor nodeid)]
        (reset! *graph* g)))
  _ (reset! last-x (+ inc_x x))
  _ (reset! last-y (+ inc_y y))
  _ (reset! last-node nodeid)
  ]
  nil)
  (Thread/sleep db/draw-sleep) 
  nil)

(defn draw-norm
  "Draws initial graph with the norm conditions, all in red"
  [norm-id]
  (m/with-mongo db/mongo-conn 
  (do
  (let [records  (m/fetch :norm-state
                            :where {:id norm-id})
        records (first records)
        norm (:norm records)
        _ (info records)

        active (:active records)
        active (into active [{:event "Active" :parameters []}])
        _ (reset! last-node (keyword "Norm"))
        _ (reset! last-x 20)
        _ (reset! last-y 270)
        _ (doall (map #(draw-node % "Repaired") active))

        repaired (:repaired records)
        repaired (into repaired [{:event "Repaired" :parameters []}])
        _ (reset! last-node (keyword "Norm"))
        _ (reset! last-x 620)
        _ (reset! last-y 270)
        _ (doall (map #(draw-node % "Repaired") repaired))


       ]nil)))
  nil)
  

(defn create-frame
  "Creation of SWING component to view graph"
  [svgcanvas]
  (let [actionspanel (JPanel. )
        frame (JFrame.)
        pane (.getContentPane frame)]
    (.setLayout actionspanel (FlowLayout.))
    (.add pane actionspanel BorderLayout/PAGE_START)
    (.add pane svgcanvas BorderLayout/CENTER)
    (.setSize frame 1000 800)
    (.setSize svgcanvas 1000 800)
    frame))

(defn mock-populate-norm-events []   
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

(defn draw-time-line
  [par-event]
  "Draw an event on the time-line"
  (let [g (deref *graph*)
        svgcanvas (:svgcanvas g)
        x (deref last-x)
        y (deref last-y)
        ancestor (deref last-node)
        event (:event par-event)
        parameters (:parameters par-event)
        id (str event "-" parameters)
        description (:description par-event)
        nodeid (keyword (gensym id))
        inc_x (if (= x 810) -800 200)
        inc_y (if (= inc_x  -800) 160 0)
        fill "burlywood"
        _ (info "Drawing: " x " " y " " event " " parameters " " description " " nodeid " " ancestor)
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid description :x x :y y :height 100 :style {:fill fill :stroke "red"})
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

(defn query-graph-time-line
  []
  "Query mongoDB looking for events inserted after last query. Orders events by time and type. 
  See collection time-line-type for types. Print events on graph"
  (m/with-mongo db/mongo-conn 
  (do
    (let [my-time (deref last-time)
          actual-time (tc/to-long (time/now))
          records  (m/fetch :time-line-mock
                            :where {:time {:$gt my-time}})
          records (sort-by (juxt :time :type) records)
         _ (info "Events retrieved:" records)
         _ (doall (map #(draw-time-line %) records))
         _ (reset! last-time actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (query-graph-time-line))
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
        _ (Thread/sleep 10000) 
        ;_ (draw-norm -1)
        ;fut (future (mock-populate-obligation-propective))
        fut (future (query-graph-time-line))
        ]
    (SwingUtilities/invokeAndWait
     (fn [] (.setVisible frame true)))))


