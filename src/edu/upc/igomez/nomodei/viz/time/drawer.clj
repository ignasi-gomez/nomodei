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

;lein run -m edu.upc.igomez.nomodei.viz.time.drawer

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/time/drawer.clj")
;(use 'edu.upc.igomez.nomodei.viz.time.drawer)
;lein run -m edu.upc.igomez.nomodei.viz.time.drawer

(ns edu.upc.igomez.nomodei.viz.time.drawer
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
      (add-node (keyword "Monitor-Start") ["Monitor" "Start"]  :x 10 :y 10 :height 100 :style {:fill "lavender" :stroke "red"})))

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

(def ^{:dynamic true} *graph* (atom (gen-graph)))

(def ^{:dynamic true} pane (atom ""))

(def last-x (atom 210))
(def last-time (atom (tc/to-long (time/now))))
(def last-y (atom 10))
(def last-node (atom (keyword "Monitor-Start")))



(defn decode-fill
  "Transforms event types to node colours"
  [type-id]
  (let [fill-map {:0 "lavender"
                  :9999 "floralwhite"
                  :8999 "burlywood"
                  :7999 "greenyellow"
                  :6999 "azure"
                  :6900 "azure"
                  :6800 "azure"
                  :6700 "azure"
                  :6600 "azure"
                  :5999 "moccasin"}
        value (get fill-map (keyword (str type-id)))
       ]value 
        ))

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
        fill (decode-fill (:type par-event))
        _ (info "Drawing: " x " " y " " event " " parameters " " description " " nodeid " " ancestor)
        actual-time (tc/to-long (time/now))
        _ (m/with-mongo db/mongo-conn 
          (do (m/insert! :event-graph
                {:x x :y y :description description :nodeid nodeid :ancestor ancestor :timestamp actual-time})
            ))
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
                            :where {:time {:$gt my-time} :line true})
          records (sort-by (juxt :time :type) records)
         _ (info "Events retrieved:" records)
         _ (doall (map #(draw-time-line %) records))
         _ (reset! last-time actual-time)
         ]nil)))
  (Thread/sleep db/query-sleep) 
  (future (query-graph-time-line))
  nil)



(defn -main []
  (let [g (deref *graph*)
        doc (:xmldoc g)
        svgcanvas (:svgcanvas g)
        jsvgscrollpane (jsvgscrollpane svgcanvas)
        _ (reset! pane jsvgscrollpane)
        updatemanager (update-manager svgcanvas)
        frame (create-frame jsvgscrollpane)
        g (build g)
        _ (reset! *graph* g)
        _ (info "Graph ready")
        _ (Thread/sleep 10000) 
        fut (future (query-graph-time-line))
        ]
    (SwingUtilities/invokeAndWait
     (fn [] (.setVisible frame true)))))


