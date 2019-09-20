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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Clojurepalooza Instructions!
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; We will be making the game snake in today's Clojurepalooza

;; Here is a template that you can follow - see how much of the game you
;; can code up in this session!

(def clicks (reagent/atom 0))

(defn dot []
  ;; 1) Fill me in to make a dot on the screen!
  ;; 2) Make your dot send an alert when you click on it
  ;; 3) Count the number of clicks on the dot, and display the total count in top right hand corner of the screen
  ;; 4) Listen for keypresses - w,a,s,d
  ;; 5) Make the dot move up, down when you click a key
  ;; 6) Make the dot move left, right when you click a key
  ;; 7) Set up a grid
  (let [click-handler
        (fn [x]
          (swap! clicks inc)
          #_(js/alert (gstring/format "Clicked the dot %d times." @clicks)))]
    [:div
     [:div {:style {:width 80
                    :height 80
                    :background-color "purple"
                    :position "absolute"}
            :onClick click-handler}]
     [:p {:style {:top 0
                  :right 10
                  :position "fixed"}}
      "Number of clicks:" @clicks]])
  )

(defn main []
  (reagent/render [dot]
                  (.getElementById js/document "app"))
  (js/document.addEventListener
   "keypress"
   (fn [event] (let [k (.-key event)]
                 (.log js/console event)
                 (.log js/console (js-keys event))))))

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
