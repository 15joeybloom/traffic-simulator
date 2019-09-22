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

(def clicks (reagent/atom 0))
(def dot-position (reagent/atom [0 0]))

(defn dot []
  (let [click-handler
        (fn [x]
          (swap! clicks inc)
          #_(js/alert (gstring/format "Clicked the dot %d times." @clicks)))
        [x y] @dot-position]
    [:div
     [:div {:style {:top (str y "px")
                    :left (str x "px")
                    :width "10px"
                    :height "10px"
                    :background-color "purple"
                    :position "absolute"}
            :onClick click-handler}]
     [:p {:style {:top 0
                  :right 10
                  :position "fixed"}}
      "Number of clicks:" @clicks]])
  )

(defn add-event-listener [event-type listener-name new-listener]
  (let [listener-name (str "my-" event-type "-listener-" listener-name)]
    (when-let [old-listener (aget js/window listener-name)]
      (.removeEventListener js/window event-type old-listener))
    (aset js/window listener-name new-listener)
    (.addEventListener js/window event-type new-listener)))

(defn my-key-listener [event]
  (let [k (.-key event)]
    #_(.log js/console (js-keys event))
    (case k
      \h (swap! dot-position (fn [p] (update p 0 #(- % 10))))
      \j (swap! dot-position (fn [p] (update p 1 #(+ % 10))))
      \k (swap! dot-position (fn [p] (update p 1 #(- % 10))))
      \l (swap! dot-position (fn [p] (update p 0 #(+ % 10)))))))

(defn main []
  (reagent/render [dot]
                  (.getElementById js/document "app"))
  (add-event-listener "keypress" :hjkl my-key-listener))

(main)

(comment
  (do
    (use 'figwheel-sidecar.repl-api)
    (start-figwheel!)
    (cljs-repl))

  )

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
