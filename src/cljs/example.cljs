(ns myapp.example
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ajax.core :refer [GET POST]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [goog.string :as gstring]
            [goog.string.format]
            [reagent.core :as reagent]
            [sablono.core :as sab]))

(.log js/console (str "----------------------\n"
                      "Starting App!"
                      "\n----------------------\n"))

(def cell-size 25)
(def grid-size 20)
(defn px [n] (str n "px"))

;; defonce'ing each piece of page state makes it persist when you live reload
;; the page.

(defonce message (reagent/atom "Snake"))

(defn random-position []
  [(rand-int grid-size) (rand-int grid-size)])

(defonce snake (reagent/atom (vec (for [i (range 9 -1 -1)] [i 0]))))
(defonce food-position (reagent/atom (random-position)))
(defonce score (reagent/atom 0))

(defn dot [{:keys [id color] [x y] :position}]
  [:div {:id "dot"
         :style {:top (px (* y cell-size))
                 :left (px (* x cell-size))
                 :width (px cell-size)
                 :height (px cell-size)
                 :background-color color
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
    [:p @message]
    [:p "Score: " @score]]
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
                               :top "50%"}}

                 [dot {:id "food"
                       :position @food-position
                       :color "yellow"}]]
                (for [position @snake]
                  [dot {:position position
                        :color "purple"}])))])

(defonce listeners (reagent/atom {}))

(defn add-event-listener [event-type listener-name new-listener]
  (when-let [old-listener (get-in @listeners [event-type listener-name])]
    (.removeEventListener js/window event-type old-listener))
  (swap! listeners assoc-in [event-type listener-name] new-listener)
  (.addEventListener js/window event-type new-listener))

(defonce direction (reagent/atom :right))
(defn my-key-listener [event]
  (let [k (.-key event)]
    #_(.log js/console (js-keys event))
    (reset! direction
            (case k
              \h :left
              \j :down
              \k :up
              \l :right
              (do (.log js/console (str "Key pressed: " k))
                  @direction)))))

(defonce timer (reagent/atom nil))

(defn move []
  (swap! snake #(vec (cons (first %) (butlast %))))
  (swap! snake
         update-in
         [0 (if (#{:left :right} @direction) 0 1)]
         (if (#{:right :down} @direction) inc dec))
  (cond
    (not (every? #(< -1 % grid-size) (first @snake)))
    (do (js/clearInterval @timer)
        (reset! message "Game over"))

    (= (first @snake) @food-position)
    (do (swap! score inc)
        (reset! food-position (random-position)))))

(defonce _ (reset! timer (js/setInterval move 100)))

(defn main []
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

;; How to add a keypress listener - be careful -- you might end up adding multiple at once!
;;    note: the listener is a function that takes a single argument
;;          if you want to "see" the argument, you can use
;;           (.log js/console event)
;;          and to see its methods, use
;;           (js-keys event)
;; (def add-keypress-listener
;;   (js/document.addEventListener "keypress"
;;                                 (fn [event] (let [k (.-key event)]))))
