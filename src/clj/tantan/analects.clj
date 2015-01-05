(ns tantan.analects
  (:require [tantan.dev :refer [is-dev?]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [environ.core :refer [env]]))

(defn analects-data-dir
  ([path] (-> (analects-data-dir)
              .toPath
              (.resolve path)
              .toFile))
  ([] (io/file
       (io/resource "analects"))))

(defn ^:private chise-ids-files []
  (filter #(.endsWith (.getName %) ".txt")
          (file-seq (analects-data-dir "chise_ids"))))

(defn ^:private read-chise-ids-file [path]
  (with-open [reader (io/reader path)]
    (doall
     (map #(string/split % #"\t")
          (filter (partial re-find #"\t")
                  (line-seq reader))))))

(defn ^:private read-cedict-file [path]
  (with-open [reader (io/reader path)]
    (doall
     (filter identity
             (map #(re-find #"^([^\s]*) ([^\s]*) \[([\w\d:,· ]+)\] (.*)" %)
                  (line-seq reader))))))

(defn ^:private chise-ids* []
  (mapcat read-chise-ids-file (chise-ids-files)))

(defn ^:private cedict* []
  (read-cedict-file (analects-data-dir "cedict/cedict_1_0_ts_utf-8_mdbg.txt")))


(def chise-ids (memoize chise-ids*))

(def cedict (memoize cedict*))

(defn chise-ids-lookup [char]
  (first (filter (fn [[_ ch _]] (= ch char)) (chise-ids))))

(defn cedict-lookup-zh [zh]
  (filter (fn [[_ zh-t zh-s _ _ _]] (or (= zh zh-t) (= zh zh-s))) (cedict)))

(def cjk-ranges [{:name  "CJK Unified Ideographs"
                  :start 0x4E00
                  :end 0xA000
                  ;; Ox9FC4..9FFF have no Unihan data
                  :sort_diff  -0x4E00}
                 {:name "CJK Unified Ideographs Extension A"
                  :start 0x3400
                  :end 0x4DC0
                  ;; 0x4DB6..4DBF have no Unihan data
                  :sort_diff  0x1E00}
                 {:name "CJK Unified Ideographs Extension B"
                  :start 0x20000
                  :end 0x2A6E0
                  ;; 0x2A6D7..2A6DF have no Unihan data
                  :sort_diff  -0x19400}
                 {:name "CJK Compatibility Ideographs"
                  :start 0xF900
                  :end 0xFB00
                  ;; 0xFADA..FAFF ; 0xFA2E..0xFA2F; 0xFA6B..0xFA6F have no Unihan data
                  :sort_diff  0xFD00}
                 {:name "CJK Compatibility Ideographs Supplement"
                  :start 0x2F800
                  :end 0x2FA20
                  ;; 0x2FA1E..0x2FA1F have no Unihan data
                  :sort_diff  -0x10000}
                 {:name "CJK Radicals supplement"
                  :start 0x2E80
                  :end 0x2F00}
                 {:name "Kangxi Radicals"
                  :start 0x2F00
                  :end 0x2FE0}])

(defn cjk-char? [char]
  (let [codepoint (long char)]
    (some #(and
            (<= (:start %) codepoint)
            (< codepoint (:end %))) cjk-ranges)))

(defn filter-cjk-chars [cs]
  (filter cjk-char? (seq cs)))

(defn chise-ids-decomp [char]
    (let [[_ _ decomp] (chise-ids-lookup char)]
      (filter #(not (= char %))
              (map str (filter-cjk-chars decomp)))))

;;(chise-ids-lookup "𣥧")
;;(rand-nth (chise-ids))
