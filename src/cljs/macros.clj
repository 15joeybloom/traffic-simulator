(ns cljs.macros)

(defmacro dbg [x]
  `(let [eval-x# ~x]
     (do (cljs.core/println '~x " = " eval-x#)
         eval-x#)))
