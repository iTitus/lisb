(ns lisb.adl.adl2lisb
  (:require [lisb.translation.util :refer :all]))

(defn assign [pc & kvs]
  {:pc (inc pc)
   :ops (fn [jump?]
          (let [opname (keyword (gensym "assign"))
                newpc (if jump? jump? (inc pc))]
            [`(bop ~opname [] (bprecondition (b= :pc ~pc)
                                             (bsequential-sub (bassign ~@kvs)
                                                              (bassign :pc ~newpc))))]))})

(defn do [pc & args]
  (loop [[instr & instrs :as allinstrs] args
         pc pc
         ops []]
    (let [res (apply (resolve (first instr)) pc (rest instr))]
      (if (seq instrs)
        (recur instrs (:pc res) (into ops ((:ops res) nil)))
        {:pc (:pc res)
         :ops (fn [jump] (into ops ((:ops res) jump)))}))))


(defn while [pc condition & body]
  (let [opname-enter (keyword (gensym "while_enter"))
        opname-exit (keyword (gensym "while_exit"))
        body-pc (inc pc)
        res (apply do body-pc body)]
    {:pc (:pc res)
     :ops (fn [jump?]
            (let [exit-pc (if jump? jump? (:pc res))]
              (into ((:ops res) pc)
                    [`(bop ~opname-enter [] (bprecondition (band (b= :pc ~pc)
                                                                 ~condition)
                                                           (bassign :pc ~body-pc)))
                     `(bop ~opname-exit [] (bprecondition (band (b= :pc ~pc)
                                                                (bnot ~condition))
                                                          (bassign :pc ~exit-pc)))])))}))
         

(defn if 
  ([pc condition then] (apply if pc condition then [nil]))
  ([pc condition then else]
   (let [opname-then (keyword (gensym "ifte-then"))
         opname-else (keyword (gensym "ifte-else"))
         then-pc (inc pc)
         then-res (apply do then-pc [then])
         else-pc (:pc then-res)
         else-res (when else (apply do else-pc [else]))
         exit-pc (if else (:pc else-res) else-pc)]
    {:pc exit-pc
     :ops (fn [jump?]
            (concat ((:ops then-res) (if jump? jump? exit-pc))
                    (when else ((:ops else-res) (if jump? jump? exit-pc)))
                    [`(bop ~opname-then [] (bprecondition (band (b= :pc ~pc)
                                                                ~condition)
                                                          (bassign :pc ~then-pc)))
                     `(bop ~opname-else [] (bprecondition (band (b= :pc ~pc)
                                                                (bnot ~condition))        
                                                          (bassign :pc ~(if else else-pc (if jump? jump? exit-pc)))))]))})))

(defmacro algorithm [& args]
  ((:ops (apply lisb.adl.adl2lisb/do 0 args)) nil))

(defmacro adl [namey & args]
  (let [vardecls (butlast args)
        algorythm (last args)
    algensalat (:ops (macroexpand algorythm))]
    `(b (apply boperations ~algensalat))))


(clojure.pprint/pprint (macroexpand '(algorithm
             (while (> :x 0)
               (assign :x (/ :x 2) :y (* :y 2))
               (if (not= 0 (mod :x 2))
                 (assign :p (+ :p :y)))
               ))))





(comment 
(def programm-counter (atom 0))

(defn get-free-pc []
  (swap! programm-counter inc))

(defn process-assign [assign machine-ctx pc next-pc]
  (let [assign-counter (:assign-counter machine-ctx)]
    (->
      machine-ctx
      (update :assign-counter inc)
      (update :operations conj (list
                                 (keyword (str "assign" assign-counter))
                                 []
                                 (list 'pre
                                       (list '= :pc pc)
                                       (list 'sequential-sub
                                             assign
                                             (list 'assign :pc next-pc))))))))

(declare process-statement)
(declare process-statements)

(defn process-if [if machine-ctx pc next-pc]
  (let [if-counter (:if-counter machine-ctx)
        next-machine-ctx (update machine-ctx :if-counter inc)
        condition (nth if 1)
        then (nth if 2)
        then-pc (get-free-pc)]
    (if (= 3 (count if))
           ; if-then
           (let [next-machine-ctx (update next-machine-ctx :operations conj
                                          (list
                                            (keyword (str "if" if-counter "_then"))
                                            []
                                            (list 'pre
                                                  (list 'and
                                                        (list '= :pc pc)
                                                        condition)
                                                  (list 'assign :pc then-pc)))
                                          (list
                                            (keyword (str "if" if-counter "_else"))
                                            []
                                            (list 'pre
                                                  (list 'and
                                                        (list '= :pc pc)
                                                        (list 'not condition))
                                                  (list 'assign :pc next-pc))))
                 next-machine-ctx (process-statement then next-machine-ctx then-pc next-pc)]
             next-machine-ctx)
           ; if-then-else
           (let [next-machine-ctx (update next-machine-ctx :operations conj
                                          (list
                                            (keyword (str "if" if-counter "_then"))
                                            []
                                            (list 'pre
                                                  (list 'and
                                                        (list '= :pc pc)
                                                        condition)
                                                  (list 'assign :pc then-pc))))
                 next-machine-ctx (process-statement then next-machine-ctx then-pc next-pc)
                 else (nth if 3)
                 else-pc (get-free-pc)
                 next-machine-ctx (update next-machine-ctx :operations conj
                                          (list
                                            (keyword (str "if" if-counter "_else"))
                                            []
                                            (list 'pre
                                                  (list 'and
                                                        (list '= :pc pc)
                                                        condition)
                                                  (list 'assign :pc else-pc))))
                 next-machine-ctx (process-statement else next-machine-ctx else-pc next-pc)]
             next-machine-ctx))))

(defn process-while [while machine-ctx pc next-pc]
  (let [while-counter (:while-counter machine-ctx)
        next-machine-ctx (update machine-ctx :while-counter inc)
        condition (nth while 1)
        body-pc (get-free-pc)
        next-machine-ctx (update next-machine-ctx :operations conj
                                 (list
                                   (keyword (str "while" while-counter "_enter"))
                                    []
                                   (list 'pre
                                         (list 'and
                                               (list '= :pc pc)
                                               condition)
                                         (list 'assign :pc body-pc)))
                                 (list
                                   (keyword (str "while" while-counter "_exit"))
                                   []
                                   (list 'pre
                                         (list 'and
                                               (list '= :pc pc)
                                               (list 'not condition))
                                         (list 'assign :pc next-pc))))
        next-machine-ctx (process-statements (drop 2 while) next-machine-ctx body-pc pc)]
    next-machine-ctx))

(defn process-statement [statement machine-ctx pc next-pc]
  (cond
    (= 'assign (first statement)) (process-assign statement machine-ctx pc next-pc)
    (= 'if (first statement)) (process-if statement machine-ctx pc next-pc)
    (= 'while (first statement)) (process-while statement machine-ctx pc next-pc)
    (= 'do (first statement)) (process-statements (rest statement) machine-ctx pc next-pc)))

(defn process-statements
  ([statements machine-ctx]
   (reset! programm-counter 0)
   (process-statements statements machine-ctx 0))
  ([statements machine-ctx pc]
   (if (empty? statements)
     machine-ctx
     (let [next-pc (get-free-pc)
           next-machine-ctx (process-statement (first statements) machine-ctx pc next-pc)]
       (recur (rest statements) next-machine-ctx next-pc))))
  ([statements machine-ctx pc last-pc]
   (if (= 1 (count statements))
     (let [last (first statements)]
       (process-statement last machine-ctx pc last-pc))
     (let [next-pc (get-free-pc)
           next-machine-ctx (process-statement (first statements) machine-ctx pc next-pc)]
       (recur (rest statements) next-machine-ctx next-pc last-pc)))))


(defn process-var [var machine-ctx]
  (-> machine-ctx
      (update :variables conj (nth var 1))
      (update :invariants conj (nth var 2))
      (update :init conj (list 'assign (nth var 1) (nth var 3)))))

(defn process-adl [definitions machine-ctx]
  (if (empty? definitions)
    machine-ctx
    (let [definition (first definitions)
          new-machine-ctx (cond
                        (= 'var (first definition)) (process-var definition machine-ctx)
                        (= 'algorithm (first definition)) (process-statements (rest definition) machine-ctx)
                        )]
      (recur (rest definitions) new-machine-ctx))))

(defn create-machine-ctx [adl]
  (assert (= 'adl (first adl)) "Algorithm-Definition-Language should look like (adl ...)")
  (process-adl (drop 2 adl) {:name (second adl)
                             :variables  [:pc]
                             :invariants ['(in :pc nat-set)]
                             :init       ['(assign :pc 0)]
                             :operations []
                             :assign-counter 0
                             :if-counter 0
                             :while-counter 0
                             :do-counter 0}))

(defn build-machine [machine-ctx]
  (list 'machine
        (:name machine-ctx)
        (list* 'variables (:variables machine-ctx))
        (list* 'invariants (:invariants machine-ctx))
        (list* 'init (:init machine-ctx))
        (list* 'operations (:operations machine-ctx)) ))

(defn adl->lisb [adl]
  (-> adl
    (create-machine-ctx)
    (build-machine)))
)