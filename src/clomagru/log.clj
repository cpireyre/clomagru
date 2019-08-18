(ns clomagru.log)

(defn timelog-stdin [& xs]
  (println (java.util.Date. (System/currentTimeMillis)) xs))
