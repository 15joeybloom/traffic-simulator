(ns circles-demo.core
  "A bunch of cars driving in a circle"
  (:require [goog.string :as gstring]
            [goog.string.format]
            [quil.core :as q]
            [quil.middleware :as m]
            [reagent.core :as r]))

(def pi Math/PI)

(def frame-rate
  "Frames per second"
  60)

(def speed-limit
  "Speed limit is 100 kph = 27.778 m/s"
  27.778)

(def road-radius
  "Cars can deal with about 1 g of lateral acceleration.

   Remembering my high school physics...
   https://courses.lumenlearning.com/physics/chapter/6-3-centripetal-force/

   a_c = v^2 / r
   =>
   r = v^2 / a_c
     = (100 km / h)^2 / (9.8 m / s^2)
     = (100000/3600 m / s)^2 / (9.8 m / s^2)
     = (771.605 m^2 / s^2) / (9.8 m / s^2)
     = 78.7 m"
  78.7)

(def road-length
  (* 2 pi road-radius))

(def reaction-time
  (r/atom 0.3))

(def comfortable-brake
  "Comfortable braking acceleration in m/s^2
  https://copradar.com/chapts/references/acceleration.html"
  4.6)

(def max-brake
  "The fastest a typical car can possibly brake is 0.8 g = 7.8 m/s^2
  https://copradar.com/chapts/references/acceleration.html"
  7.8)

(def max-acceleration
  "VW Passat can accelerate as fast as about 14 kph/s = 4 m/s^2
  https://accelerationtimes.com/models/vw-passat-variant-2-0-tdi"
  4)

(def lane-width
  "https://en.wikipedia.org/wiki/Lane#Lane_width"
  3.7)

(def car-width
  "https://en.wikipedia.org/wiki/Volkswagen_Passat_(B8)"
  1.83)

(def background-color
  [190 255 50])

(defn html-color [[r g b]]
  (gstring/format "rgb(%s,%s,%s)" r g b))

(defn draw [{:keys [width height cars]}]
  (let [center-x (/ width 2)
        center-y (/ height 2)
        road-radius-px (* 0.8 (min center-x center-y))
        m->px (/ road-radius-px road-radius)
        road-width-px (* m->px lane-width)]
    (apply q/background background-color)
    (q/fill 0 0) ;; road circle fill is transparent
    (q/stroke-weight road-width-px)
    (q/stroke 200) ;; gray road
    (q/ellipse center-x center-y (* road-radius-px 2) (* road-radius-px 2))
    (doseq [{:keys [id position velocity]} cars
            :let [x (+ center-x
                       (* road-radius-px
                          (Math/cos (* 2 pi (/ position road-length)))))
                  y (+ center-y
                       (* road-radius-px
                          (Math/sin (* 2 pi (/ position road-length)))))]]
      (q/stroke-weight 0)
      (if (< (+ velocity 5) speed-limit)
        ;; blue if car is slower than speed limit, i.e. "stuck in traffic"
        (q/fill 0 0 255)
        ;; red if car is near speed limit
        (q/fill 255 0 0)
        )
      (q/ellipse x y (* m->px car-width) (* m->px car-width))
      (q/fill 0) ;; black text
      (q/text-num id x (+ y 20)))))

(defn conj-history
  "Add an item to the history, pushing old items out if the history is
  full."
  [{:keys [size-fn items] :as history} item]
  (let [size (size-fn)
        items' (conj items item)]
    (assoc history :items (if (> (count items') size)
                            (vec (take-last size items'))
                            items'))))

(defn stopping-distance
  "https://en.wikipedia.org/wiki/Braking_distance#Rules_of_thumb"
  [velocity]
  (let [x (/ velocity 10)]
    (* x (+ x 3))))

(defn update-car
  [t dt
   {:keys [id position velocity acceleration history] :as car}
   {next-car :history next-id :id}]
  (let [position' (mod (+ position (* velocity dt)) road-length)
        velocity' (max (+ velocity (* acceleration dt)) 0)
        d (mod (- (:position (first (:items next-car)))
                  (:position (first (:items history))))
               road-length)
        d' (mod (- (:position (last (:items next-car)))
                   (:position (last (:items history))))
                road-length)
        dd (- d' d)
        acceleration'
        (cond (< d' (stopping-distance velocity)) (- comfortable-brake)
              (> velocity speed-limit) -1
              (< velocity speed-limit) max-acceleration
              :else 0)]
    (-> car
        (assoc :acceleration acceleration'
               :velocity velocity'
               :position position')
        (update :history conj-history {:t t :position position'}))))

(defn rotate
  "Stick the first element of `coll` at the end"
  [[x & xs]]
  (concat xs [x]))

(defn update-state [{:keys [t width height cars] :as state}]
  (let [t' (/ (q/frame-count) frame-rate)
        dt (- t' t)
        cars' (map (partial update-car t' dt) (rotate cars) cars)]
    (assoc state :t t' :cars cars')))

(defn reaction-time-history-size []
  (int (* @reaction-time frame-rate)))

(def initial-cars
  (vec (for [i (range 40)
             :let [x (- (rand) (* 10 i))]]
         {:id i
          :position x ; m along the circular road
          :velocity 10 ; m/s
          :acceleration 0 ; m/s^s
          :history {:size-fn reaction-time-history-size
                    :items [{:t 0 :position x}]}
          })))

(defn init [width height]
  (fn []
    (q/frame-rate frame-rate)
    {:t 0
     :width width
     :height height
     :cars initial-cars}))

(defn canvas []
  (r/create-class
   {:component-did-mount
    (fn [component]
      (let [node (r/dom-node component)
            width (.-innerWidth js/window)
            height (.-innerHeight js/window)]
        (q/sketch
         :host node
         :draw draw
         :setup (init width height)
         :update update-state
         :size [width height]
         :middleware [m/fun-mode])))
    :render
    (fn [] [:div {:style {:position "absolute"
                          :top 0
                          :bottom 0
                          :left 0
                          :right 0}}])}))

(defn slider [ratom min max]
  [:input {:type "range" :value @ratom :min min :max max :step 0.01
           :on-change (fn [e]
                        (let [new-value (js/parseFloat (.. e -target -value))]
                          (reset! ratom new-value)
                          (print (reaction-time-history-size))))}])

(defn home-page []
  (r/with-let [running? (r/atom false)]
    [:div {:style {:position "absolute"
                   :top 0
                   :bottom 0
                   :left 0
                   :right 0
                   :background-color (html-color background-color)}}
     [:div {:style {:position "absolute"
                    :z-index 1}}
      [:button {:on-click #(swap! running? not)}
       (if @running? "stop" "start")]
      [:br]
      "Reaction time: " @reaction-time "s"
      [slider reaction-time 0.0 5.0]]
     (when @running?
       [canvas])]))

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
