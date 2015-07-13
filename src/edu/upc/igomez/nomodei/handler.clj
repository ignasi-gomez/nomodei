;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Copyright (c) 2013 SUPERHUB - SUstainable and PERsuasive Human Users moBility
;                    http://www.superhub-project.eu/ 
; 
; All rights reserved. This program and the accompanying materials
; are made available under the terms of the Eclipse Public License v1.0
; which accompanies this distribution, and is available at
; http://www.eclipse.org/legal/epl-v10.html
; 
; Contributors:
;     Ignasi Gómez-Sebastià - Development of policy encoder server skeleton 
;                             (2013-03-24) (yy-mm-dddd)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(ns edu.upc.igomez.nomodei.handler
   (:use compojure.core
        [clojure.tools.logging :only (info error)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.response :as response]
            [clojure.data.json :as json]
            [edu.upc.igomez.nomodei.db.db :as db])
  (:import (java.lang Integer)))

(defroutes app-routes
    (GET "/" [] (str "NoMoDEI Server successfully started. Refer to documentation on README.md for more information.\n"
                     "IT Support information:\n"
                     "  · DB Connection status:" (db/db-info)))
  
  (route/resources "/")
  (route/not-found "Invalid endpoint. Refer to documentation on README.md for more information"))

(def app
  (handler/site app-routes))
