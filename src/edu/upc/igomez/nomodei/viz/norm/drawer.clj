;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2015 Ignasi Gómez Sebastià
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Code to draw norm instance states based on the events recieved on the
; visualization DB. 
;
; Contributors:
;     Ignasi Gómez-Sebastià - First Version (2015-07-30) (yyyy-mm-dd)
;                             
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;lein run -m edu.upc.igomez.nomodei.viz.norm.drawer -6665

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/norm/drawer.clj")
;(use 'edu.upc.igomez.nomodei.viz.norm.drawer)

(ns edu.upc.igomez.nomodei.viz.norm.drawer
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

(defn gen-graph
  "Draws initial graph with the node Monitor Start"
  []
  (-> (graph)
       (add-node (keyword "Norm") ["Drawing Norm" "Please Wait"]  :x 10 :y 400 :width db/norm-node-width :height db/norm-node-height :style {:fill "lavender" :stroke "red"})))

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

(def last-time-viz (atom 0))
(def last-node (atom (keyword "Norm")))

(defn draw-norm-line
  [records]
  "Draw a node of the norm line"
  (m/with-mongo db/mongo-conn 
  (do
    (let [g (deref *graph*)
          svgcanvas (:svgcanvas g)
          x (:x records)
          y (:y records)
          ancestor (:ancestor records)
          ancestor (keyword ancestor)
          nodeid (:nodeid records)
          nodeid (keyword nodeid)
          description (:description records)
          height (:height records)
          width (:width records)
          fill (:fill records)
          _ (info "Drawing: " x " " y " " description " " nodeid " " ancestor " " fill)
      _ (do-batik-update
        g
        (let [g (add-node! g nodeid description :x x :y y :height height :width width :style {:fill fill :stroke "red"})
              ;g (add-edge! g (keyword (gensym "edge")) ancestor nodeid)
              ;g (set-node-selected! g nodeid true)
              ]
          (reset! *graph* g)))
    ]
    nil)))
  (Thread/sleep db/draw-sleep) 
  nil)
  
  (defn draw-norm
    "Draw a whole norm line"
    [norm-id]
    (m/with-mongo db/mongo-conn 
      (do
        (let [records  (m/fetch :norm-instance-graph-node
                                  :where {:norm-instance-id norm-id }
                                  :sort {:timestamp 1})
              _ (info "Maldades" records)
              _ (doall (map #(draw-norm-line %) records))
        ]
        nil)))
    nil)

(defn update-norm-viz-node
  [records]
  "Redraws a particular node in the norm visualization as it has been updated"
  (let [g (deref *graph*)
        x (:x records)
        y (:y records)
        ancestor (:ancestor records)
        ancestor (keyword ancestor)
        nodeid (:nodeid records)
        nodeid (keyword nodeid)
        description (:description records)
        height (:height records)
        width (:width records)
        fill (:fill records)
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid description :x x :y y :height height :width width :style {:fill fill :stroke "red"})]
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
                            :where {:timestamp {:$gt my-time} :norm-instance-id norm-id})
         _ (info "Nodes retrieved:" records)
         _ (doall (map #(update-norm-viz-node %1) records))
         _ (reset! last-time-viz actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (update-norm-viz norm-id))
  nil)

(defn -main [& args]
  (let [norm-id (first args)
        norm-id (read-string norm-id)
        g (deref *graph*)
        doc (:xmldoc g)
        svgcanvas (:svgcanvas g)
        jsvgscrollpane (jsvgscrollpane svgcanvas)
        _ (reset! pane jsvgscrollpane)
        updatemanager (update-manager svgcanvas)
        frame (create-frame jsvgscrollpane)
        g (build g)
        _ (reset! *graph* g)
        _ (SwingUtilities/invokeAndWait
          (fn [] (.setVisible frame true)))
        _ (Thread/sleep 20000) 
        _ (info "Pelusso " norm-id)
        _ (draw-norm norm-id)
        _ (Thread/sleep 10000) 
        fut (future (update-norm-viz norm-id))
        ]
    ))


