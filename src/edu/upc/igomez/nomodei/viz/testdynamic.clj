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

;lein run -m edu.upc.igomez.nomodei.viz.testdynamic

(ns edu.upc.igomez.nomodei.viz.testdynamic
  (:use lacij.edit.graph
        lacij.edit.dynamic
        analemma.xml
        [clojure.tools.logging :only (info error)]
        (tikkba swing dom core)
        tikkba.utils.xml)
  (:import org.apache.batik.apps.svgbrowser.DOMViewer
           (javax.swing SwingUtilities JFrame JButton BoxLayout JPanel Timer)
           (java.awt.event ActionListener)
           (java.awt Rectangle)
           (org.apache.batik.swing JSVGCanvas JSVGScrollPane)
           (java.awt Component BorderLayout FlowLayout)))


(defn gen-graph
  []
  (-> (graph)
      (add-node (keyword "Monitor\nStart") "Monitor\nStart" :x 10 :y 10)))

(def ^{:dynamic true} *graph* (atom (gen-graph)))

(def ^{:dynamic true} pane (atom ""))

(def last-x (atom 210))
(def last-y (atom 10))
(def last-event (atom 0))
(def last-node (atom (keyword "Monitor\nStart")))

(defn timer-action
  []
  (let [g (deref *graph*)
        svgcanvas (:svgcanvas g)
        x (deref last-x)
        y (deref last-y)
        event (str (deref last-event))
        ancestor (deref last-node)
        nodeid (keyword (gensym event))
        inc_x (if (= x 810) -800 200)
        inc_y (if (= inc_x  -800) 80 0)
        _ (info "Pelusso " x " " y " " event " " nodeid " " ancestor)
    _ (do-batik-update
      g
      (let [g (add-node! g nodeid event :x x :y y)
            g (add-edge! g (keyword (gensym "edge")) ancestor nodeid)
            g (set-node-selected! g nodeid true)]
        (reset! *graph* g)))
  _ (reset! last-x (+ inc_x x))
  _ (reset! last-y (+ inc_y y))
  _ (reset! last-node nodeid)
  _ (swap! last-event inc)
  ]
  nil)
  (Thread/sleep 1000) 
  (future (timer-action))
  nil)

(defn create-frame
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
        _ (Thread/sleep 10000) 
        fut (future (timer-action))
        ]
    (SwingUtilities/invokeAndWait
     (fn [] (.setVisible frame true)))))