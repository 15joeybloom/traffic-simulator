(ns cljs.snake
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cljs.macros :refer [dbg]])
  (:require [goog.string :as gstring]
            [goog.string.format]
            [reagent.core :as reagent]))

(.log js/console (str "----------------------\n"
                      "Starting App!"
                      "\n----------------------\n"))

;; defonce'ing each piece of page state makes it persist when you live reload
;; the page.

(defonce car (reagent/atom 0))

(defn car [[x y]]
  [:div {:style {:top (px (* y cell-size))
                 :left (px (* x cell-size))
                 :width (px cell-size)
                 :height (px cell-size)
                 :background-color (case @(get-cell [x y])
                                     :empty "rgba(0, 0, 0, 0)"
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
                               :width (px (* board-size cell-size))
                               :height (px (* board-size cell-size))

                               ;; center the grid on the page
                               ;;
                               :transform "translate(-50%, -50%)"
                               :position "absolute"
                               :left "50%"
                               :top "50%"}}]
                (for [p grid-positions]
                  [dot p])))])

(defonce listeners (reagent/atom {}))

(defn add-event-listener! [event-type listener-name new-listener]
  (when-let [old-listener (get-in @listeners [event-type listener-name])]
    (.removeEventListener js/window event-type old-listener))
  (swap! listeners assoc-in [event-type listener-name] new-listener)
  (.addEventListener js/window event-type new-listener))

(defonce desired-direction (reagent/atom :right))

(def key-map {\h :left
              \j :down
              \k :up
              \l :right
              \w :up
              \a :left
              \s :down
              \d :right})
(defn my-key-listener [event]
  (let [k (.-key event)]
    #_(.log js/console (js-keys event))
    (if (contains? key-map k)
      (reset! desired-direction (key-map k))
      (.log js/console (str "Not in key map: "k)))))

(defonce timer (reagent/atom nil))

(defonce current-direction (reagent/atom :right))
(defonce snake (reagent/atom (vec (for [i (range 9 -1 -1)] [i 0]))))
(defonce food-position (reagent/atom nil))

(defn render! [old-snake new-snake]
  (doseq [p old-snake]
    (reset! (get-cell p) :empty))
  (doseq [p new-snake]
    (reset! (get-cell p) :snake)))

(defn grow-snake! [f]
  "Grow the snake from its head. The location of the new head is the result of
  applying `f` to the current head. Returns the coordinates of the new head and
  the cell contents before the new head was there."
  (let [[new-head] (swap! snake #(vec (cons (f (first %)) %)))]
    [new-head (first (reset-vals! (get-cell new-head) :snake))]))

(defn shrink-snake! []
  "Shrink the snake from its tail."
  (let [[old-snake] (swap-vals! snake butlast)]
    (reset! (get-cell (last old-snake)) :empty)
    nil))

(defn move! []
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
        [new-head what-we-ate]
        (grow-snake! #(update % coordinate-to-update how-to-update))]
    (if (= what-we-ate :food)
      (do (swap! score inc)
          (reset! food-position (random-food-position))
          (reset! (get-cell @food-position) :food))
      (shrink-snake!))
    (reset! current-direction next-direction)
    (when (not (every? #(< -1 % board-size) new-head))
      (do (js/clearInterval @timer)
          (reset! message "Game over")))))

(defonce _ (reset! timer (js/setInterval move! 100)))

(defn main []
  (render! [] @snake)
  (reset! food-position (random-food-position))
  (reset! (get-cell @food-position) :food)

  (reagent/render [snake-game]
                  (.getElementById js/document "app"))
  (add-event-listener! "keypress" :hjkl my-key-listener))

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
