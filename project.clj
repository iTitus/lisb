(defproject org.clojars.pkoerner/lisb "0.0.5-SNAPSHOT"
  :description "lisb: A Clojure library that wraps ProB's constraint solver. Use it to write/transform B machines with less pain or interact with the constraint solver."
  :url "https://github.com/pkoerner/lisb" ;; subject to change
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["snapshot" "https://oss.sonatype.org/content/repositories/snapshots"]
                 ["release" "https://oss.sonatype.org/content/repositories/releases"]]
  :deploy-repositories [["releases"  {:sign-releases false :url "https://repo.clojars.org/"}]
                        ["snapshots" {:sign-releases false :url "https://repo.clojars.org/"}]]
  :jvm-opts ["-Xss1g"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/math.combinatorics "0.3.0"]
                 [potemkin "0.4.7"]
                 [com.rpl/specter "1.1.4"]
                 [clj-wallhack "1.0.1"]
                 [de.hhu.stups/de.prob2.kernel "4.13.0"]])
