(ns lisb.translation.eventb.dsl
  (:require [clojure.walk :refer [walk]]
            [lisb.translation.lisb2ir :refer [b band bparallel-sub]]
            [clojure.spec.alpha :as s]))

(defn eventb-context [name & clauses]
  {:tag :context 
   :name name 
   :machine-clauses clauses})

(defn eventb-machine [name & clauses]
  {:tag :machine
   :name name
   :machine-clauses clauses})

(defn eventb-events [& events]
  {:tag :events
   :values events})

(defn eventb-then [& actions]
  [:body (bparallel-sub actions)])

(defn eventb-with [& witnesses]
  [:witness (band witnesses)])

(defn eventb-when [& gurads]
  [:guard (band gurads)])

(defn eventb-status [status]
  [:status status])

(defn eventb-any [& args]
  [:args args])

(defn eventb-refines [event]
  [:refines event])

(defn eventb-event [name & clauses] 
  (into {:name name
         :status :ordinary
         :gurad true
         :witness true} 
        clauses))

(defmacro eventb [lisb]
    `(b (let [~'axioms ~'properties
              ~'theorems ~'assertions
              ~'context eventb-context
              ~'machine eventb-machine
              ~'events eventb-events
              ~'event eventb-event
              ~'when eventb-when
              ~'any eventb-any
              ~'then eventb-then
              ~'refines eventb-refines
              ~'with eventb-with
              ~'status eventb-status
              ] 
         ~lisb)))

(comment 
  (eventb (machine :machine-foo
                   (variables :x :y :z)
                   (events 
                    (event :foo1 
                           (any :t)
                           (then 
                            (assign :x :t)
                            (becomes-such :y (> :y :t))
                            (becomes-element-of :z :nat)))
                    (event :foo2 
                           (when (< 0 :x 10))
                           (then
                            (assign :x :y) 
                            (becomes-such :y (> :y 10)) 
                            (becomes-element-of :z :nat)))
                    (event :foo3
                           (refines :foo1)
                           (when (< 0 :x 10))
                           (with (in :t :nat))
                           (then
                            (assign :x :y)
                            (becomes-such :y (> :y 10))
                            (becomes-element-of :z :nat)))))
    )
  )


