function anagrams
- given an anagram key `ak`, an initial set of partial angrams `pas`
  
  e.g `ak` = "adeor", `pas` = #{"a", "are", "dare" ...}
  
  - For each partial anagram `pa` in `pas`,
    - Let `key-diff` be the multiset difference between `pa` and `ak`

      e.g `pa` = "a", `key-diff` = "deaor"

      - If `pas` contains `key-diff`, then [`pa` `key-diff`] is a solution, and we can move on to the next iteration of `pa`
      - Else, `pa` _may_ be a solution, but we have to check by calling anagrams recursively with the leftovers, that is, the partial anagrams we can make from the multiset difference between `pa` and `key-diff`
        - This is where I start to get lost... How do I conver this to a recursive function?


