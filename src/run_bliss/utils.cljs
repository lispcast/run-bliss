(ns run-bliss.utils)

(defn distance [lat1 lon1 lat2 lon2]
  (let [radlat1 (-> lat1
                  (/ 180)
                  (* js/Math.PI))
        radlat2 (-> lat2
                  (/ 180)
                  (* js/Math.PI))
        theta (- lon1 lon2)
        radtheta (-> theta
                   (/ 180)
                   (* js/Math.PI))
        dist (+ (* (js/Math.sin radlat1)
                  (js/Math.sin radlat2))
               (* (js/Math.cos radlat1)
                 (js/Math.cos radlat2)
                 (js/Math.cos radtheta)))]
    (-> dist
      (js/Math.acos)
      (* (/ 180 js/Math.PI)
        60
        1.1515))))

(defn distance-gps [gps1 gps2]
  (distance (get-in gps1 [:coords :latitude])
            (get-in gps1 [:coords :longitude])
            (get-in gps2 [:coords :latitude])
            (get-in gps2 [:coords :longitude])))

(defn distance-gps-list [ls]
  (let [pairs (partition 2 1 ls)
        distances (map #(apply distance-gps %) pairs)]
    (reduce + 0 distances)))
