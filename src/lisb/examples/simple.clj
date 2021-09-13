(ns lisb.examples.simple
  (:use [lisb.translation.representation])
  (:use [lisb.high-level]))

(def lift (b (machine
               (machine-variant)
               (machine-header :Lift [])
               (variables :etage)
               (invariant (contains? (range 0 100) :etage))
               (init (assign :etage 4))
               (operations
                (operation [] :inc [] (pre (< :etage 99) (assign :etage (+ :etage 1))) )
                (operation [] :dec[] (pre (> :etage 0) (assign :etage (- :etage 1))))))))

(def lift2 (b {:name :Lift
              :variables #{:etage}
              :invariants (contains? (range 0 100) :etage)
              :init (assign :etage 4)
              :operations #{{:name :inc
                             :result ()
                             :parameters ()
                             :body (pre (< :etage 99) (assign :etage (+ :etage 1)))}
                             {:name :dec
                              :result ()
                              :parameters ()
                              :body (pre (> :etage 0) (assign :etage (- :etage 1)))}}}))

(def a-counter (b (machine
                    (machine-variant)
                    (machine-header :ACounter [])
                    (variables :ii :jj)
                    (invariant (and (contains? (range 0 11) :ii)
                                     (contains? (range 0 11) :jj)
                                     (< :ii 11)
                                     (>= :jj 0)))
                    (init (assign :ii 2 :jj 10))
                    (operations
                      (operation [] :inc [] (select (> :jj 0) (parallel-substitution
                                                                (assign :ii (+ :ii 1))
                                                                (assign :jj (- :jj 1)))))
                      (operation [:result] :res [] (assign :result :ii))))))

(def gcd (b (machine
              (machine-variant)
              (machine-header :GCD [])
              (variables :x :y)
              (invariant (and (contains? nat-set :x) (contains? nat-set :y)))
              (init (parallel-substitution (assign :x 70) (assign :y 40)))
              (operations
                (operation [:s] :GCDSolution [] (if-sub (= :y 0) (assign :s :x) (assign :s -1)))
                (operation [] :Step [] (if-sub (> :y 0) (parallel-substitution (assign :x :y) (assign :y (mod :x :y)))))
                (operation [] :Restart [:w1 :w2] (pre (and (contains? nat1-set :w1) (contains? nat1-set :w2))
                                                      (if-sub (> :w1 :w2)
                                                              (assign :x :w1 :y :w2)
                                                              (assign :y :w1 :x :w2))))))))

(def knights-knaves (b (machine
                         (machine-variant)
                         (machine-header :KnightsKnaves [])
                         (constants :A :B :C)
                         (properties (and
                                       (contains? bool-set :A)
                                       (contains? bool-set :B)
                                       (contains? bool-set :C)
                                       (<=> (= :A true) (or (= :B false) (= :C false)))
                                       (<=> (= :B true) (= :A true)))))))

(def bakery0 (b (machine
                  (machine-variant)
                  (machine-header :Bakery0 [])
                  (variables :aa)
                  (invariant (contains? (range 0 3) :aa))
                  (init (assign :aa 0))
                  (operations
                    (operation [] :enter1 [] (select (= :aa 0) (assign :aa 1)))
                    (operation [] :enter2 [] (select (= :aa 0) (assign :aa 2)))
                    (operation [] :leave1 [] (select (= :aa 1) (assign :aa 0)))
                    (operation [] :leave2 [] (select (= :aa 2) (assign :aa 0)))
                    (operation [] :try1 [] skip)
                    (operation [] :try2 [] skip)))))

(def bakery1 (b (machine
                  (machine-variant)
                  (machine-header :Bakery1 [])
                  (variables :p1 :p2 :y1 :y2)
                  (invariant (and (contains? (range 0 3) :p1)
                                   (contains? (range 0 3) :p2)
                                   (contains? natural-set :y1)
                                   (contains? natural-set :y2)
                                   (=> (= :p1 2) (< :p2 2))
                                   (=> (= :p2 2) (< :p1 2))))
                  (init (assign :p1 0 :p2 0 :y1 0 :y2 0))
                  (operations
                    (operation [] :try1 [] (select (= :p1 0) (parallel-substitution (assign :p1 1) (assign :y1 (+ :y2 1)))))
                    (operation [] :enter1 [] (select (and (= :p1 1) (or (= :y2 0) (< :y1 :y2))) (assign :p1 2)))
                    (operation [] :leave1 [] (select (= :p1 2) (parallel-substitution (assign :p1 0) (assign :y1 0))))
                    (operation [] :try2 [] (select (= :p2 0) (parallel-substitution (assign :p2 1) (assign :y2 (+ :y1 1)))))
                    (operation [] :enter2 [] (select (and (= :p2 1) (or (= :y1 0) (< :y2 :y1))) (assign :p2 2)))
                    (operation [] :leave2 [] (select (= :p2 2) (parallel-substitution (assign :p2 0) (assign :y2 0))))))))

(def smalltrace
  (let [m (load-initialized-machine-trace bakery0)]
    (-> m
        (perform :enter1)
        (perform :try1)
        (perform :leave1))))

(def smalltrace-next-steps
  (possible-ops smalltrace))

(comment
  this wants to be a snake game example when grown up.
  (def snek (b (machine
               (machine-variant)
               (machine-header :Snek [])
               (variables :board :direction)
               (invariant
                (and
                                        ;types
                 (member :board (total-fn
                                 (* (range 1 9) (range 1 9))
                                 {0 1}))
                 (member :direction (* (range -1 1) (range -1 1)))))
               (init (assign :board )) ; oh. how do i do set comprehension?
               ))))

(comment

  (perform smalltrace (.getName (first smalltrace-next-steps)))

  (create-ns 'b)

  (intern 'b
          'take
          btake)
         )
