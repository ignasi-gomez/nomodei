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
;(use 'edu.upc.igomez.nomodei.viz.mockups.timeline)


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
(def last-y (atom 120))
(def last-node (atom (keyword "Norm")))

(defn draw-norm-line
  [par-event par-line]
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
    (let [records  (m/fetch :norm-state
                            :where {:id norm-id})
          records (first records)
          norm (:norm records)
          _ (info records)

          active (:active records)
          active (concat [{:event "Active" :parameters []}] active)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 10)
          _ (doall (map #(draw-norm-line % "Repaired") active))

          violated (:violated records)
          violated (concat [{:event "Violated" :parameters []}] violated)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 210)
          _ (doall (map #(draw-norm-line % "Violated") violated))

          fulfilled (:fulfilled records)
          fulfilled (concat [{:event "Fulfilled" :parameters []}] fulfilled)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 410)
          _ (doall (map #(draw-norm-line % "Fulfilled") fulfilled))

          repaired (:repaired records)
          repaired (concat [{:event "Repaired" :parameters []}] repaired)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 610)
          _ (doall (map #(draw-norm-line % "Repaired") repaired))

          compensated (:compensated records)
          compensated (concat [{:event "Compensated" :parameters []}] compensated)
          _ (reset! last-node (keyword "Norm"))
          _ (reset! last-x 210)
          _ (reset! last-y 810)
          _ (doall (map #(draw-norm-line % "Compensated") compensated))

          

         ]nil))) 
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
        ]
    (SwingUtilities/invokeAndWait
     (fn [] (.setVisible frame true)))))


