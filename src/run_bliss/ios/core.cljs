(ns run-bliss.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [run-bliss.events]
            [run-bliss.subs]
            [run-bliss.utils :as utils]
            [cljs.pprint :as pprint]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [current-gps (subscribe [:current-gps])
        current-run (subscribe [:current-run])
        app-state (subscribe [:app-state])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 20 :margin-bottom 20}}
        "Speed: "
        (.toFixed (* 2.23694 (get-in @current-gps [:coords :speed])) 2)
        " mph"]
       [text {:style {:font-size 20 :margin-bottom 20}}
        "Pace: "

        (.toFixed (/ 60 (* 2.23694 (get-in @current-gps [:coords :speed]))) 2)

        " minutes / mile"]

       (if @current-run
         [view {}
          (let [date (js/Date. (:start-time @current-run))]
            [text {}
             "Run started: "
             (.getHours date)
             ":"
             (.getMinutes date)
             ":"
             (.getSeconds date)])
          [text {}
           "Distance: "
           (.toFixed (utils/distance-gps-list (:gps-readings @current-run)) 2)
           " miles"]
          [touchable-highlight
           {:style {:background-color "#999" :padding 10 :border-radius 5}
            :on-press #(let [end-time (.getTime (js/Date.))]
                         (dispatch [:stop-run end-time]))}
           [text
            {:style {:color "white" :text-align "center" :font-weight "bold"}}
            "stop run"]]]
         [touchable-highlight
          {:style {:background-color "#999" :padding 10 :border-radius 5}
           :on-press #(let [run-id (random-uuid)
                            start-time (.getTime (js/Date.))]
                        (dispatch [:start-run run-id start-time]))}
          [text
           {:style {:color "white" :text-align "center" :font-weight "bold"}}
           "start run"]])


       [view {}
        [text {}
         (with-out-str (pprint/pprint @current-gps))]
        [text {}
         (:status @current-run)]
        [text {}
         (count (:gps-readings @current-run))]]])))

(defn save-position [position]
  (let [pos (js->clj position :keywordize-keys true)]
    (dispatch [:record-current-gps pos])
    (dispatch [:record-run-gps     pos])))

(defn init []
  (js/navigator.geolocation.watchPosition
    (fn [position]
      (save-position position))
    (fn [])
    #js {:enableHighAccuracy true
         :timeout 20000
         :maximumAge 1000
         :distanceFilter 10})
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RunBliss" #(r/reactify-component app-root)))
