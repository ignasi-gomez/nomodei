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
