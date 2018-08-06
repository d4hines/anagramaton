(ns anagramaton.core-test
  (:require [clojure.test :refer :all]
            [anagramaton.core :as a]
            [trie.core :refer [trie]]
            [multiset.core :as ms]))

(deftest clean-string
  (is (= (a/clean-string "A' s") "as")))

(deftest clean-string
  (is (= (a/anagram-key "adore") "adeor"))
  (is (= (a/anagram-key "zyyyx") "xyyyz")))

(deftest dict->word-map
  (let [dict ["read" "dare" "boo"]
        expected {"ader" #{"read" "dare"} "boo" #{"boo"}}
        result (a/dict->word-map dict)]
    (is (=  result expected))))

(deftest partial-anagram?
  (is (a/partial-anagram? "foobar" "oob"))
  (is (a/partial-anagram? "foobar" "rfa"))
  (is (not (a/partial-anagram? "foo" "foob"))))

(deftest partial-anagram-keys
  (testing "Should only return a, i, and o as single letter words"
    (let [dict ["a" "b" "c" "i" "o"]
          word-map (a/dict->word-map dict)]
      (is (= (a/partial-anagram-keys word-map "abcio")
             #{"a" "i" "o"}))))

  (testing "Besides single-letter words, should return all partial anagrams"
    (let [dict ["adore" "dare" "read" "red"]
          word-map (a/dict->word-map dict)]
      (is (= (a/partial-anagram-keys word-map "adore")
             #{"adeor" "ader" "der"})))))

(def ms ms/multiset)

(deftest ms-subtract
  (testing "Should subtract any number of multisets from minuend"
    (is (= (a/ms-subtract (ms :a :b :c) (ms :b) (ms :c))
           (ms :a))))
  (testing "Should return ::not-subet if the sum of the subtrehends is not a subset of the minuend"
    (is (= (a/ms-subtract (ms :a :b :c) (ms :a :a))
           ::a/not-subset))))

(def word-map (a/dict->word-map ["adore" "dare" "read" "o"]))
(def paks (a/partial-anagram-keys word-map "adore"))
(def t (trie paks))

(deftest anagram-helper
  (testing "On base case where the input multiset is empty, should add the partial solution to the solutions list"
    (is (= (a/anagram-helper (trie) a/empty-ms ["foo"] [])
           [["foo"]])))
  (testing "Should return every anagram key for a given multiset and partial-anagram key set trie"
    (let [word-map word-map
          paks paks
          t t]
      (is (= (a/anagram-helper t (a/str->ms "adore") [] [])
             `(["ader" "o"] ["adeor"]))))))

(deftest anagrams
  (testing "Should return no anagrams for impossible anagrams"
    (let [word-map word-map]
      (is (= (a/anagrams word-map "qwer")
             #{}))))
  (testing "Should return every anagram of a given string"
    (let [word-map word-map]
      (is (= (set (a/anagrams word-map "adore"))
             #{`("dare" "o") `("read" "o") `("adore")})))))

