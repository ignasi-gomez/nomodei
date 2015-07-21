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

;lein run -m edu.upc.igomez.nomodei.viz.test

;(load-file "/Users/igomez/deapt/dea-repo/nomodei/src/edu/upc/igomez/nomodei/viz/test.clj")
;(use 'edu.upc.igomez.nomodei.viz.test)


(ns edu.upc.igomez.nomodei.viz.test
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
            [edu.upc.igomez.nomodei.db.db :as db])
  (:import (javax.swing JFrame JButton BoxLayout SwingUtilities)
           (java.awt.event ActionListener)
           java.awt.Component))

;; copied from swing utils
(defn add-action-listener
  "Adds an ActionLister to component. When the action fires, f will be
invoked with the event as its first argument followed by args.
Returns the listener."
  [component f & args]
  (let [listener (proxy [ActionListener] []
                   (actionPerformed [event] (apply f event args)))]
    (.addActionListener component listener)
    listener))


(defn add-nodes [g & nodes]
  (reduce (fn [g node]
            (add-node g node (name node)))
          g
          nodes))

;(take 100 (repeat (generate-random-line)))
(defn generate-random-line 
  []
  {:event "Pelusso" :norm "Maldades" :time (tc/to-long (time/now))}
  )

(defn gen-graph-dynamic
  []
  (-> (graph)
      (add-default-node-attrs :width 25 :height 25 :shape :circle)
      (add-nodes (take 100 (repeat (generate-random-line))))
      #_(add-edges [:r :s] [:s :t] [:s :u] [:s :v]
                 [:t :t1] [:t :t2] [:t :t3] [:t :t4] [:t :t5]
                 [:u :u1] [:u :u2] [:v :v1] [:v :v2] [:v :v3]
                 [:r :w] [:w :w1] [:w :w2] [:w :y]
                 [:y :y3] [:y :y2] [:y :y1]
                 [:r :x] [:x :x1] [:x :x2] [:x :x3] [:x :x4] [:x :x5])
      (build)))

(defn on-action1 [event svgcanvas g]
  (do-batik
   svgcanvas
   (-> g
       (add-node! :appolon "Appolon" :x 50 :y 350)
       (add-edge! :appolon-athena :appolon :athena))))

(defn on-action2 [event svgcanvas g]
  (do-batik
   svgcanvas
   (-> g
       (add-node! :appolon "Pelusson" :x 50 :y 350)
       (add-edge! :appolon-athena :appolon :athena))))

(defn gen-graph-gods
  []
  (-> (graph)
      (add-node :athena "Athena" :x 10 :y 30)
      (add-node :zeus "Zeus" :x 200 :y 150)
      (add-node :hera "Hera" :x 500 :y 150)
      (add-node :ares "Ares" :x 350 :y 250)
      (add-node :matrimony "♥" :x 400 :y 170 :shape :circle)
      (add-edge :father1 :athena :zeus)
      (add-edge :zeus-matrimony :zeus :matrimony)
      (add-edge :hera-matrimony :hera :matrimony)
      (add-edge :son-zeus-hera :ares :matrimony)
      (build)))


(defn gen-graph-cyclic
  []
  (-> (graph :width 800 :height 600)
      (add-default-node-attrs :width 25 :height 25 :shape :circle)
      (add-node :n1 "n1")
      (add-node :n2 "n2")
      (add-node :n3 "n3")
      (add-node :n4 "n4")
      (add-edge :e12 :n1 :n2)
      (add-edge :e23 :n2 :n3)
      (add-edge :e31 :n3 :n1)
      (add-edge :e24 :n2 :n4)
      (build)
      ))

(defn gen-graph
  []
  (gen-graph-gods))

(defn create-frame
  [svgcanvas g]
  (let [frame (JFrame.)
        button1 (JButton. "Action1")
        button2 (JButton. "Action2")
        pane (.getContentPane frame)]
    (add-action-listener button1 on-action1 svgcanvas g)
    (add-action-listener button2 on-action2 svgcanvas g)
    (.setLayout pane (BoxLayout. pane BoxLayout/Y_AXIS))
    (.setAlignmentX button1 Component/CENTER_ALIGNMENT)
    (.setAlignmentX button2 Component/CENTER_ALIGNMENT)
    (.add pane button1)
    (.add pane button2)
    (.add pane svgcanvas)
    (.setSize frame 800 600)
    (.setSize svgcanvas 800 600)
    frame))

(defn -main []
  (let [g (-> (gen-graph)
              (layout :hierarchical)
              )
        doc (:xmldoc g)
        _ (export g "/tmp/tmp.svg")
        svgcanvas (:svgcanvas g)
        frame (create-frame svgcanvas g)]
    (SwingUtilities/invokeAndWait
     (fn [] (.setVisible frame true)))))


