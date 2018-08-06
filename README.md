# anagramaton [![CircleCI](https://circleci.com/gh/d4hines/anagramaton.svg?style=svg)](https://circleci.com/gh/d4hines/anagramaton)

A library for generating and manipulating anagrams.

> CEIINOSSITTUU | UT TENSIO, SIC UIS
> - Robert Hooke, _De Potentia Restitutiva_
## Usage
[![Clojars Project](https://img.shields.io/clojars/v/d4hines/anagramaton.svg)](https://clojars.org/d4hines/anagramaton)

```clojure
(require '[d4hines/anagramaton.core :as a])
;; The library requires a dictionary as input, in the form of an array strings. Here's an example.
(def words (str/split (slurp "/usr/share/dict/words") #"\n"))
;; Next we need to do some preproccessing on the dictionary
(def word-map (a/dict->word-map words))
;; Now you can start forming anagrams!
(a/anagrams word-map "read")
=> #{["Dear"]
     ["Dare"]
     ["Read"]
     ["A" "Red"]
     ["Ad" "Re"]}
```

## Background
Pyschology researchers are investigating subjects' ability to solve anagrams. As such, they need a way of generating and verifying the solution to anagrams.


## Definitions

For the purpose of this project, let the following definitions hold:
- Word: One of the set of standard [Unix words](https://en.wikipedia.org/wiki/Words_(Unix)).
- Anagram: a word or sequence of words formed by rearranging the letters of another string, such as _rant creep_, formed from _carpenter_.
- Anagram Puzzle: a string which is not a word but is an anagram.
- Anagram Solution: a word made of the same letters of a given anagram.
- Unsolvable Anagram: a string which is neither a word nor an anagram (bit of misnomer, eh?).

## Requirements

The solution must:
- Be a clojure library...
- That can generate anagram puzzles..
- Of varying difficulty...
- And verify that a given word is the solution to a given anagram...
- While being fast enough to use interactively at the REPL.

## Prior Art

### Internet Anagram Server by Wordsmith

Wordsmith's service sports an impressive number of knobs you can turn to create great anagrams. 
This thing is awesome. So tempted to just make client library for their api and be done...

### Scrabble
https://en.wikipedia.org/wiki/Scrabble_letter_distributions
Who would have ever thought 


### The Anagram Dictionary https://en.wikipedia.org/wiki/Anagram_dictionary

### Other Honorable Mentions
http://norvig.com/mayzner.html - Really good explanations, updates to bigram tables
https://stackoverflow.com/a/12477976 - basic algorithm for finding single word anagrams
http://pi.math.cornell.edu/~mec/2003-2004/cryptography/subs/digraphs.html - Bigram table I'm using
https://web.stanford.edu/class/cs9/sample_probs/Anagrams.pdf - good explanation of algorithm
http://www.ssynth.co.uk/~gay/anagabout.html - implementation, but with shoddy details

https://stackoverflow.com/a/881367 - inspiration 


Thanks to @seancorfield, @dpsutton, and @porkostomus for help with the partial anagram algorithm!


## Questions and Answers

- Are we going to deal with Anagram phrases (multiword anagrams)?
  - Every definition I've read so far defines an anagram as a "word, _phrase_, or name". While I'm not excited about the added complexity multi-word anagrams will add (orders of magnitude more), I think they have to be handled to stay true to the problem.
- What are we going to do to optimize for speed?
  - As little as possible. [Premature optimization](http://wiki.c2.com/?PrematureOptimization) and all that.
- How are we going to measure "difficulty"?
  - See next section.

## The Difficulty with Difficulty

How the heck do you measure anagram difficulty? It turns out this is a subject of much academic research and debate.

 Here's the quick and dirty summary of my research so far:
- Mayzner and Tresselt seem to be the historic authorities on the matter, and are cited by many studies like [this one](https://link.springer.com/content/pdf/10.3758/BF03196922.pdf). 
  - They have a ton of studies that examine the effects of different studies, e.g [this one](https://www.researchgate.net/publication/9978351_Anagram_solution_time_A_function_of_letter_order_and_frequency).
- A lot of this went over my head, but it seems that bigram frequency is one of the 
