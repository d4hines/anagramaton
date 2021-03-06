(ns anagramaton.core
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.math.combinatorics :as combo]
            [trie.core :refer [trie]]
            [multiset.core :as ms]))

(defn partial-anagram1? [word subword]
  (->> [word subword]
       (map #(apply ms/multiset (seq %)))
       (apply ms/subset?)))

(defn partial-anagram? [word subword]
  (let [f-hist (frequencies word)
        sub-hist (frequencies subword)]
    (and (set/subset? (set (keys sub-hist)) (set (keys f-hist)))
         (every? second (merge-with >= f-hist sub-hist)))))

(partial-anagram? "hello" "hello")

(defn clean-string [x] (-> x
                           str/lower-case
                           (str/replace #"\W|'" "")))

(defn anagram-key [x]
  (-> x vec sort str/join))

(def wordset (as-> "/usr/share/dict/words" x
               (slurp x)
               (str/split x #"\n")
               ;; For Dev only, remember to delete!
               ;; (take 1000 x)
               (map clean-string x)
               (set x)))

;; Map of all single word anagrams in the word set
(def word-map (reduce (fn [agg curr]
                        (update agg
                                (anagram-key curr)
                                #(into #{curr} %)))
                      {} wordset))

(def anagram-set (set (map anagram-key wordset)))

(defn partial-anagrams
  [word]
  (reduce (fn [matches [ana-key ana-words]]
            (if (partial-anagram? word ana-key)
              (into matches ana-words)
              matches))
          #{} word-map))

(defn partial-anagram-keys
  [word]
  (reduce (fn [matches [ana-key ana-words]]
            ;; only allow a, i, and o as one-leter words
            (if (and (or (> (count ana-key) 1)
                         (contains? #{"a" "i" "o"} ana-key))
                     (partial-anagram? word ana-key))
              (conj matches ana-key)
              matches))
          #{} word-map))

(defn bigrams
  "create bigrams out of a string"
  [^String strn]
  (loop [os strn rs #{}]
    (if (<= (.length os) 2)
      (conj rs os)
      (recur (subs os 1)
             (conj rs (subs os 0 2))))))

(defn partial-anagram-keys-multiset
  [x]
  (->> x
       partial-anagram-keys
       (map (partial apply ms/multiset))
       set))

(def adore-key (anagram-key "adore"))

(defn str->ms [s] (apply ms/multiset (seq s)))

(def ak-ms (str->ms adore-key))

(def paks (partial-anagram-keys adore-key))

(def paks-ms (partial-anagram-keys-multiset "adore"))

(def deor (apply ms/multiset (seq "deor")))
(def aer (apply ms/multiset (seq "aer")))
(def adoe (apply ms/multiset (seq "adoe")))
(def a (apply ms/multiset (seq "a")))

(map #(ms/minus ak-ms %) paks-ms)

;; not a solution
(contains? paks-ms (ms/minus ak-ms adoe))

;; a solution
(contains? paks-ms (ms/minus ak-ms aer))
(map str/join [(ms/minus ak-ms aer) aer])

(let [pa a
      mdf (ms/minus ak-ms pa)
      ;; if mdf and pa form a solution, then NONE of the others can.
      ;; However, they may contain partial solutions
      mdf-pa-is-solution? (contains? paks-ms mdf)
      sub-partial (partial-anagram-keys-multiset (str/join mdf))]
  (if mdf-pa-is-solution?
    (map str/join [mdf pa])))

(let [pa a
      mdf (ms/minus ak-ms pa)
      ;; if mdf and pa form a solution, then NONE of the others can.
      ;; However, they may contain partial solutions
      mdf-pa-is-solution? (contains? paks-ms mdf)
      sub-partial (partial-anagram-keys-multiset (str/join mdf))]
  (if mdf-pa-is-solution?
    (map str/join [mdf pa])))

(comment

  (defn anagram [letters]
    (let [tiles ms/multiset (seq letters)
          min-length (count letters)]
      (_anagram tiles [] root min-length)))

  (defn _anagram
    [tiles path root min-length]
    (let [trie-node (root path)
          depth (count path)
          final (= trie-node path)])
    (if (and final (>= depth min-length)))))

(defn ms-subtract
  ([a b]
   (if (ms/subset? b a)
     (ms/minus a b)
     ::not-subset))
  ([a b & more]
   (reduce ms-subtract
           (ms-subtract a b)
           more)))

(defn str-ms-subtract [mset s]
  (ms-subtract mset (str->ms s)))

(def t (trie paks))

(def empty-ms (ms/multiset))

(defn anagram-helper0
  [letter t mset solution]
  (let [matches (t letter)
        current-match (first matches)
        ms-diff
        (if (empty? matches)
          ::no-match
          (str-ms-subtract mset current-match))
        working-solution (conj solution current-match)]
    (case ms-diff
      empty-ms working-solution
      ::no-match ::no-match
      (let [first-letter (str (first current-match))]
        (anagram-helper0 first-letter t ms-diff working-solution)))))

(defn anagram-helper
  [current-match t mset solution]
  (let [ms-diff
        (->> current-match
             ;; convert it to a multiset
             str->ms
             ;; return the difference between the word
             (ms-subtract mset))
        working-solution (conj solution current-match)]
    [ms-diff working-solution]))


(defn pass-helper2
  [t new-ms partial-solution solutions]
  (cond (= new-ms empty-ms) (conj solutions partial-solution)
        (= new-ms ::no-match) (println "pass-helper - hello from no-match")
        :else (let [letter (str (first new-ms))
                    matches (t letter)
                    current-match (first matches)]
                (reduce (fn [agg current-match]
                          (let [[m nms sol]
                                (anagram-helper
                                 current-match t new-ms partial-solution)
                                output (if (not= nms ::not-subset)
                                         (pass-helper2 t nms sol solutions))]
                            (println "passhelper" [m nms sol output])
                            (if output (conj agg output)
                                agg))) [] matches))))

(contains? anagram-set "ar")

(reduce (fn [agg curr] (if (odd? curr(conj agg (inc curr))))) [] [1 2 3 4])

(pass-helper2 t ak-ms [] [])

(pass-helper2 t (str->ms "deo") ["ar"] [])

(defn helper2 [word mset]
  (let [options (t word)]
    (zipmap
     (map vector options)
     (map (partial str-ms-subtract mset) options))))

{["a"] #{\d \e \o \r},
 ["aor"] #{\d \e},
 ["ar"] #{\d \e \o},
 ["aer"] #{\d \o},
 ["ad"] #{\e \o \r},
 ["ader"] #{\o},
 ["adeor"] #{},
 ["ado"] #{\e \r},
 ["ador"] #{\e}}

(let [[possible-sol msdif] (-> (helper2 "a" ak-ms) vec (nth 2))]
  [possible-sol msdif])

(def pass1 (anagram-helper "a" t ak-ms []))

(def pass2 (let [[prev-word new-ms solution next-letter] pass1]
             (anagram-helper next-letter t new-ms solution)))

(def pass3 (let [[prev-word new-ms solution next-letter] pass2]
             (anagram-helper next-letter t new-ms solution)))

(anagram-helper "a" t ak-ms [])

(ms-subtract ak-ms (str->ms "z"))

(defn anagram2
  [letters]
  (let [ak (anagram-key letters)
        ak-ms (str->ms ak)
        paks (partial-anagram-keys ak)
        paks-ms (partial-anagram-keys-multiset ak)
          ;; t is a trie structure of every partial anagram key
          ;; You can ask it for every paks that begins with a given string
        t (trie paks)]
    (->> ak first str
         t
         first
         str->ms
         (ms-subtract ak-ms))))

(anagram2 "adore")

((anagram2 "adore") "a")

(comment
  (case result
    (ms/multiset) (conj ...)
    :not-subset
    proceed somehow))
