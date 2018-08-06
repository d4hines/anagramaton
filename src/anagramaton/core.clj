(ns anagramaton.core
  (:require [clojure.string :as str]
            [clojure.math.combinatorics :as combo]
            [trie.core :refer [trie]]
            [multiset.core :as ms]))

(defn clean-string
  "Lowercases and removes whitespace and apostrophes from a string.

  KNOWN ISSUE: This get's carried away sometimes. For example, it replaces \"Buñuel\" with buuel."
  [x]
  (-> x
      str/lower-case
      (str/replace #"\W|'" "")))

(defn anagram-key
  "Sorts a string alphabetically. The value of the anagram key is the same for all other anagrams, e.g (anagram-key \"read\") and (anagram-key \"dare\") are equal."
  [x]

  (-> x vec sort str/join))

(defn dict->word-map
  "Takes a dictionary of words in the form of a collection of strings and returns a map of anagram keys and the words associated with that key.

  Example usage: (dict->word-map (str/split (slurp \"/usr/share/dict/words\") #\"\\n\"))"
  [dict]
  (->> dict
       set
       (map clean-string)
       (reduce (fn [agg curr]
                 (update agg
                         (anagram-key curr)
                         #(into #{curr} %))) `{})))

(defn partial-anagram?
  "Returns true if word contains at least all the letter in subword."
  [word subword]
  (->> [subword word]
       (map #(apply ms/multiset (seq %)))
       (apply ms/subset?)))

(defn partial-anagrams
  "Returns all the words that can be formed from any combination of the letters in word.

  Note: not used by the rest of library, but useful for playing with anagrams in general."
  [word-map word]
  (reduce (fn [matches [ana-key ana-words]]
            (if (partial-anagram? word ana-key)
              (into matches ana-words)
              matches))
          #{} word-map))

(defn partial-anagram-keys
  "Returns all the partial anagrams of word in sorted key form."
  [word-map word]
  (reduce (fn [matches [ana-key ana-words]]
            ;; The unix dictionary has an entry for every letter of the alphabet.
            ;; This makes things messy, so as a workaround we filter out any
            ;; one-letter words besides a, i, and o.
            (if (and (or (> (count ana-key) 1)
                         (contains? #{"a" "i" "o"} ana-key))
                     (partial-anagram? word ana-key))
              (conj matches ana-key)
              matches))
          #{} word-map))

(defn str->ms [s] (apply ms/multiset (seq s)))

(defn ms-subtract
  "Returns the multiset difference between a and b (or any number of multisets) if b is a subset of a, otherwise returns ::not-subset."
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

(def empty-ms (ms/multiset))

(defn anagram-helper
  "Recursively searches through the trie for words, using the multiset difference to narrow the search.Returns every combination of anagram keys that can "
  [t mset partial-solution solutions]
  (if (= mset empty-ms) (conj solutions partial-solution)
      (let [letter (str (first mset))
            matches (t letter)
            output
            (map (fn [current-match]
                   (let [sol (conj partial-solution current-match)
                         nms (str-ms-subtract
                              mset current-match)]
                     (if (not= nms ::not-subset)
                       (anagram-helper t nms sol solutions)))) matches)]
        (->> output
             (filter identity)
             (mapcat identity)))))

(defn anagrams
  "Returns every anagram word or phrase that can be formed from the given letters as a sequence of word sequences."
  [word-map letters]
  (let [ak (anagram-key (clean-string letters))
        ak-ms (str->ms ak)
        paks (partial-anagram-keys word-map ak)
        ;; A trie structure of every partial anagram the letters can form.
        t (trie paks)
        akeys (anagram-helper t ak-ms [] [])]
    ;; anagram-helper returns combination of anagram keys, not words.
    ;; Therefore, it's necessary to map the keys back to the corresponding words.
    (set (mapcat (fn [x] (->> x
                              (map #(get word-map %))
                              (apply combo/cartesian-product)))
                 akeys))))

