(ns run-bliss.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [clojure.spec :as s]
   [run-bliss.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :set-greeting
 validate-spec
 (fn [db [_ value]]
   (assoc db :greeting value)))

(reg-event-db
 :record-current-gps
 validate-spec
 (fn [db [_ gps-reading]]
   (assoc db :current-gps gps-reading)))

(reg-event-db
  :record-run-gps
  validate-spec
  (fn [db [_ gps-reading]]
    (let [run-id (:current-run db)]
      (if run-id
        (update-in db [:runs run-id :gps-readings] conj gps-reading)
        db))))

(reg-event-db
  :start-run
  validate-spec
  (fn [db [_ run-id start-time]]
    (-> db
      (assoc-in [:runs run-id] {:id run-id
                                :start-time start-time
                                :gps-readings []
                                :status :started})
      (assoc-in [:current-run] run-id))))

(reg-event-db
  :stop-run
  validate-spec
  (fn [db [_ end-time]]
    (if-let [run-id (:current-run db)]
      (-> db
        (assoc-in [:runs run-id :end-time] end-time)
        (assoc-in [:runs run-id :status] :ended)
        (dissoc :current-run))
      db)))
