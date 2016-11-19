(ns run-bliss.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :app-state
  (fn [db _]
    db))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
  :current-gps
  (fn [db _]
    (:current-gps db)))

(reg-sub
  :current-run
  (fn [db _]
    (when-let [run-id (:current-run db)]
      (get-in db [:runs run-id]))))
