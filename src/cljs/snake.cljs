(ns cljs.snake
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cljs.macros :refer [dbg]])
  (:require [goog.string :as gstring]
            [goog.string.format]
            [reagent.core :as reagent]))

(.log js/console (str "----------------------\n"
                      "Starting App!"
                      "\n----------------------\n"))

(def cell-size 24)
(defn px [n] (str n "px"))

;; defonce'ing each piece of page state makes it persist when you live reload
;; the page.

(defonce message (reagent/atom "Snake"))

(defonce grid-size 25)
(def grid-positions (for [i (range grid-size)
                          j (range grid-size)]
                      [i j]))
(defn random-position []
  [(rand-int grid-size) (rand-int grid-size)])

(defonce grid (vec (for [_ (range grid-size)]
                     (vec (for [_ (range grid-size)]
                            (reagent/atom :empty))))))

(defonce score (reagent/atom 0))

(defn dot [[x y]]
  [:div {:style {:top (px (* y cell-size))
                 :left (px (* x cell-size))
                 :width (px cell-size)
                 :height (px cell-size)
                 :background-color (case @(get-in grid [x y])
                                     :empty "black"
                                     :snake "purple"
                                     :food "yellow")
                 :position "absolute"}}])

(defn snake-game []
  [:div {:style {:background-color "black"
                 :position "absolute"
                 :width "100%"
                 :height "100%"}}
   [:div {:style {:top 0
                  :margin "50px auto"
                  :text-align "center"
                  :font "4vw Courier New"
                  :color "white"}}
    [:p @message " | Score: " @score]]
   (vec (concat [:div {:id "grid"
                       :style {:border-color "white"
                               :border-width (px 5)
                               :border-style "solid"
                               :width (px (* grid-size cell-size))
                               :height (px (* grid-size cell-size))

                               ;; center the grid on the page
                               ;;
                               :transform "translate(-50%, -50%)"
                               :position "absolute"
                               :left "50%"
                               :top "50%"}}]
                (for [p grid-positions]
                  [dot p])))])

(defonce listeners (reagent/atom {}))

(defn add-event-listener [event-type listener-name new-listener]
  (when-let [old-listener (get-in @listeners [event-type listener-name])]
    (.removeEventListener js/window event-type old-listener))
  (swap! listeners assoc-in [event-type listener-name] new-listener)
  (.addEventListener js/window event-type new-listener))

(defonce desired-direction (reagent/atom :right))

(def key-map {\h :left
              \j :down
              \k :up
              \l :right})
(defn my-key-listener [event]
  (let [k (.-key event)]
    #_(.log js/console (js-keys event))
    (if (contains? key-map k)
      (reset! desired-direction (key-map k))
      (.log js/console (str "Not in key map: "k)))))

(defonce timer (reagent/atom nil))

(defonce current-direction (reagent/atom :right))
(defonce snake (reagent/atom (vec (for [i (range 9 -1 -1)] [i 0]))))
(defonce food-position (reagent/atom (random-position)))

(defn render []
  (doseq [p @snake]
    (reset! (get-in grid p) :snake))
  (reset! (get-in grid @food-position) :food))

(defn move []
  (let [desired-direction-val @desired-direction
        current-direction-val @current-direction
        next-direction (if (or (and (#{:left :right} desired-direction-val)
                                    (#{:left :right} current-direction-val))
                               (and (#{:up :down} desired-direction-val)
                                    (#{:up :down} current-direction-val)))
                         current-direction-val
                         desired-direction-val)
        coordinate-to-update (if (#{:left :right} next-direction) 0 1)
        how-to-update (if (#{:right :down} next-direction) inc dec)
        [old-snake grown-snake]
        (swap-vals! snake #(vec (cons (update (first %)
                                              coordinate-to-update
                                              how-to-update)
                                      %)))]
    (if (= (first grown-snake) @food-position)
      (do (swap! score inc)
          (reset! (get-in grid @food-position) :empty)
          (reset! food-position (random-position))
          (reset! (get-in grid @food-position) :food))
      (do (swap! snake butlast)
          (reset! (get-in grid (last grown-snake)) :empty)))
    (reset! current-direction next-direction)
    (if (not (every? #(< -1 % grid-size) (first grown-snake)))
      (do (js/clearInterval @timer)
          (reset! message "Game over"))
      (render))))

(defonce _ (reset! timer (js/setInterval move 100)))

(defn main []
  (render)

  (reagent/render [snake-game]
                  (.getElementById js/document "app"))
  (add-event-listener "keypress" :hjkl my-key-listener))

(main)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some useful examples
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Example of how to make a DOM element
;; [:div.myclass [:p "My paragraph text]]

;; Example of how to format a DOM element
;; [:div {:style {:width "800px"}}]

;; Styles you'll need to make a small dot on the screen
;; {:width FILLMEIN
;;  :height FILLMEIN
;;  :background-color FILLMEIN
;;  :position "absolute"}

;; Example of how to store application data
;; This works exactly like a clojure atom (reset!, swap!)
;; (def n-clicks (reagent/atom 0))

;; How to add an onClick handler
;; [:div {:onClick (fn [x] (js/alert "Hello!"))}]

;; How to add a keypress listener - be careful -- you might end up adding
;; multiple at once!
;;    note: the listener is a function that takes a single argument
;;          if you want to "see" the argument, you can use
;;           (.log js/console event)
;;          and to see its methods, use
;;           (js-keys event)
;; (def add-keypress-listener
;;   (js/document.addEventListener "keypress"
;;                                 (fn [event] (let [k (.-key event)]))))
