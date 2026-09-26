# Algorithm Theory — Accenture Senior Java Developer Prep

Theory behind the patterns matching the reported Accenture HackerRank questions (Majority Element, interval/CPU-core overlap, string/password validators, general array/hash problems) plus the SQL basics needed for the Spring Boot track.

---

## 1. Boyer-Moore Voting Algorithm (Majority Element)

**Matches:** the reported "Majority Element" question — find the value appearing more than N/2 times.

**Core idea:** if an element occurs more than N/2 times, all remaining elements combined occur fewer times than it does. So the majority element can never be fully "cancelled out" by the rest.

**Two-phase algorithm:**
1. **Find a candidate (single pass):** keep a `candidate` and a `count`.
   - If `count == 0`, set `candidate` = current element.
   - If current element == `candidate`, `count++`; else `count--`.
   - Intuition: every decrement effectively pairs off and eliminates one occurrence of the candidate with one occurrence of a different element. Since the majority element outnumbers everything else combined, it can't be eliminated entirely — whatever survives at the end is the majority candidate.
2. **Verify (second pass):** count actual occurrences of `candidate` and confirm it's > N/2. (Skippable if the problem guarantees a majority element exists.)

**Complexity:** O(N) time, O(1) space — this is the whole reason it beats a hash-map frequency count (O(N) time but O(N) space).

**Watch out for:** the n/3 variant (elements appearing more than N/3 times — up to 2 such elements can exist, requires tracking 2 candidates/counts).

---

## 2. Sweep Line Algorithm / Interval Scheduling

**Matches:** the reported "minimum CPU cores so no processes overlap" question, and the general Meeting Rooms II pattern.

**Core idea:** imagine a vertical line sweeping left→right across time. Every interval start is a "+1" event (a resource is now needed), every interval end is a "-1" event (a resource frees up). The **peak concurrent count** across the sweep is the minimum number of resources (rooms/CPU cores) needed.

**Standard approach:**
1. Split each interval into two events: `(start, +1)` and `(end, -1)`.
2. Sort all events by time. **Tie-breaking rule matters:** if a process ends at time 3 and another starts at time 3, decide whether that counts as overlapping (process the "end" event before the "start" event at the same timestamp if closed intervals `[start, end]` don't overlap when touching; process "start" before "end" if they do — read the problem's inclusivity rules carefully, this exact wording was called out as a gotcha in the Accenture version).
3. Sweep through sorted events, maintaining a running counter: increment on start, decrement on end.
4. Track the maximum value the counter reaches — that's the answer.

**Alternative implementation:** sort intervals by start time, use a **min-heap** of end times. For each new interval, if its start ≥ the heap's smallest end time, pop that room (it's free) and reuse it; otherwise allocate a new room (push). The heap's final size is the room count.

**Complexity:** O(N log N) for sorting (or heap operations), O(N) space.

---

## 3. Two Pointers & Sliding Window

**Matches:** the "password sanitizer"/string-filtering style question, and general substring/subarray problems (e.g., Longest Substring Without Repeating Characters).

**Two pointers:** two indices moving through a structure (often an array or string), either:
- **Converging** (from both ends toward the middle) — used for problems like two-sum-on-sorted-array or palindrome checks, requires the data to have some monotonic/sorted property.
- **Same-direction** (both moving forward, one trailing the other) — this is the basis of the sliding window.

**Sliding window:** a specialization of two pointers where you maintain a contiguous "window" `[left, right]` over an array/string:
1. Expand `right` to grow the window, updating some running state (a count, a sum, a character-frequency map).
2. When the window violates a constraint (e.g., a repeated character, a sum too large), shrink from `left` until it's valid again.
3. Track the best window size/value seen.

**Why it matters:** converts an O(n²) or O(n³) brute-force (checking every substring/subarray) into O(n), since each pointer only moves forward, so total pointer movement is bounded by 2n.

**Complexity:** O(n) time, O(1) or O(k) space (k = alphabet/character-set size if using a frequency map).

---

## 4. Hash Tables / Hashing

**Matches:** Two Sum, Group Anagrams, Top K Frequent Elements, and general "have I seen this before" checks (duplicates, frequency counts) — the backbone of most Accenture array/string questions.

**Core idea:** a hash function maps a key to an array index (bucket) so that insertion, lookup, and deletion average O(1), instead of O(n) for a linear scan.

**Collision handling** (rarely needed to implement yourself in interviews, but good to know):
- **Separate chaining:** each bucket holds a small list/tree of all keys that hashed there.
- **Open addressing:** on collision, probe for the next free slot according to some rule.

**Practical interview usage:**
- Use a `HashMap<Value, Index>` / `HashSet` to check "have I seen this value" in O(1) instead of nested loops.
- Use a `HashMap<Key, Count>` for frequency counting (majority element alternative, anagrams, top-K problems).
- **Complexity to state out loud:** average-case O(1) per operation, worst-case O(n) if many collisions (interviewers generally accept assuming O(1) average).

---

## 5. Dynamic Programming Basics — Kadane's Algorithm (Maximum Subarray)

**Matches:** Maximum Subarray, and the general DP-lite category (Climbing Stairs, Fibonacci) that shows up in the generic Accenture OA pool.

**Core idea (optimal substructure):** the max subarray sum *ending at index i* is either:
- just `nums[i]` on its own, or
- `nums[i] + (max subarray sum ending at i-1)` — but only if that previous sum is positive; otherwise it drags the total down and you're better off starting fresh at `i`.

**Recurrence:**
```
current_sum = max(nums[i], current_sum + nums[i])
max_sum = max(max_sum, current_sum)
```

**Why it's DP:** you build the answer for position `i` directly from the already-computed answer for position `i-1` — a classic bottom-up 1D DP, just with the array collapsed to two rolling variables instead of a full table (this is "DP with O(1) space" instead of the more verbose O(n) DP-array version).

**Complexity:** O(n) time, O(1) space.

**Related simple DP pattern (Climbing Stairs/Fibonacci):** `ways(n) = ways(n-1) + ways(n-2)` — same rolling-variable trick applies. Good warm-up for recognizing "this problem's answer at step n depends only on the last 1-2 steps."

---

## 6. SQL: Joins and GROUP BY

**Matches:** the reported basic SQL join question, relevant since the role touches MySQL/Oracle/MS SQL directly.

**Join types** — the only real difference between them is what happens to rows with **no matching partner** on the other side:
- **INNER JOIN:** keep only rows that match in both tables. Rows with no partner are dropped entirely.
- **LEFT (OUTER) JOIN:** keep all rows from the left table; unmatched right-side columns come back as `NULL`.
- **RIGHT (OUTER) JOIN:** mirror of LEFT — keep all rows from the right table, `NULL` for unmatched left-side columns.
- **FULL (OUTER) JOIN:** keep all rows from both sides; `NULL` fills in wherever there's no match on either side.
- **CROSS JOIN:** Cartesian product — every row of table A paired with every row of table B (no ON condition).

**GROUP BY with aggregation:** after joining tables together, `GROUP BY` collapses rows sharing a key into one row per group, so you can apply aggregate functions (`COUNT`, `SUM`, `AVG`, `MAX`, `MIN`) per group — e.g., "count employees per manager" after joining `employees` to `managers`.

**Key gotcha to remember:** any column in the `SELECT` list that isn't inside an aggregate function must appear in the `GROUP BY` clause, or the query is invalid (in strict SQL modes).

**`HAVING` vs `WHERE`:** `WHERE` filters rows *before* grouping; `HAVING` filters groups *after* aggregation (e.g., "only managers with more than 5 reports").

---

## 7. Palindromes

**Matches:** Palindrome Number, Valid Palindrome — a recurring "easy fundamentals" category in the general Accenture OA pool, and a natural extension of the two-pointer pattern already needed for the password-validator question.

**What counts as a palindrome:** a sequence that reads the same forwards and backwards. For strings, this usually means after lowercasing and stripping non-alphanumeric characters; for numbers, after treating the digits as a sequence (no actual string conversion needed if you want O(1) space).

**Core technique — two pointers converging from both ends:**
1. Place `left` at the start, `right` at the end.
2. Compare `s[left]` and `s[right]` (skipping/normalizing invalid characters for the string variant).
3. If they differ, it's not a palindrome — return false immediately.
4. If they match, move `left++` and `right--` and repeat until the pointers meet or cross.

This is O(n) time and O(1) extra space — no need to reverse the string/number and compare, though that's a valid brute-force fallback (also O(n), but uses O(n) extra space for the reversed copy).

**For numbers specifically:** you can avoid converting to a string entirely by reversing only the second half of the number mathematically (`reversedHalf = reversedHalf * 10 + n % 10`, then divide `n` by 10 each step) until `n <= reversedHalf`, then compare the two halves. Also remember: negative numbers and numbers ending in 0 (other than 0 itself) can never be palindromes — a quick early return.

**Harder variant — Valid Palindrome II:** the string may still be a palindrome if you're allowed to delete **at most one** character. Approach: run the standard two-pointer scan; the moment you hit a mismatch, try skipping either the left character or the right character and check if *that* substring is a palindrome (two-pointer check again). If either works, return true.

**Different family — Longest Palindromic Substring:** here you're not checking one candidate, you're searching for the best one among all substrings. Two standard approaches:
- **Expand around center:** for every possible center (each single character, and each gap between two characters — to cover both odd- and even-length palindromes), expand outward while the two sides keep matching, tracking the longest expansion found. O(n²) time, O(1) space.
- **Dynamic programming:** build a table `dp[i][j]` = true if `s[i..j]` is a palindrome, using the recurrence `dp[i][j] = (s[i] == s[j]) && dp[i+1][j-1]`. O(n²) time and space — same time complexity as expand-around-center but uses more memory, so expand-around-center is generally preferred unless you need to answer many substring-palindrome queries.

---

## Suggested study order

1. Hash tables (foundation for almost everything else)
2. Two pointers / sliding window
3. Palindromes (direct extension of two pointers — cheap to add once you know that pattern)
4. Kadane's / basic DP
5. Boyer-Moore voting (quick, high-value, exact match to a reported question)
6. Sweep line / interval scheduling (exact match to a reported question, slightly more involved)
7. SQL joins + GROUP BY (separate from the coding round, but equally likely to be tested)

For each pattern: read the theory above, then solve the matching LeetCode problems from the **Exercises** section below, timing yourself to the assessment's ~20-min-per-problem pace.

---

## Exercises (all LeetCode problems found so far, by topic)

### Boyer-Moore Voting / Majority Element
- [Majority Element](https://leetcode.com/problems/majority-element/) — Easy

### Sweep Line / Interval Scheduling
- [Meeting Rooms](https://leetcode.com/problems/meeting-rooms/) — Easy
- [Meeting Rooms II](https://leetcode.com/problems/meeting-rooms-ii/) — Medium
- [Merge Intervals](https://leetcode.com/problems/merge-intervals/) — Medium

### Two Pointers / Sliding Window
- [Longest Substring Without Repeating Characters](https://leetcode.com/problems/longest-substring-without-repeating-characters/) — Medium
- [Valid Parentheses](https://leetcode.com/problems/valid-parentheses/) — Easy
- [Missing Number](https://leetcode.com/problems/missing-number/) — Easy

### Palindromes
- [Palindrome Number](https://leetcode.com/problems/palindrome-number/) — Easy
- [Valid Palindrome](https://leetcode.com/problems/valid-palindrome/) — Easy
- [Valid Palindrome II](https://leetcode.com/problems/valid-palindrome-ii/) — Easy
- [Longest Palindromic Substring](https://leetcode.com/problems/longest-palindromic-substring/) — Medium
- [Longest Palindrome](https://leetcode.com/problems/longest-palindrome/) — Easy

### Hash Tables / Arrays
- [Two Sum](https://leetcode.com/problems/two-sum/) — Easy
- [Group Anagrams](https://leetcode.com/problems/group-anagrams/) — Medium
- [Top K Frequent Elements](https://leetcode.com/problems/top-k-frequent-elements/) — Medium

### Dynamic Programming Basics
- [Best Time to Buy and Sell Stock](https://leetcode.com/problems/best-time-to-buy-and-sell-stock/) — Easy
- [Maximum Subarray](https://leetcode.com/problems/maximum-subarray/) — Medium (Kadane's algorithm)
- [Climbing Stairs](https://leetcode.com/problems/climbing-stairs/) — Easy

### Linked Lists & Trees (verbal/whiteboard round)
- [Reverse Linked List](https://leetcode.com/problems/reverse-linked-list/) — Easy
- [Binary Tree Inorder Traversal](https://leetcode.com/problems/binary-tree-inorder-traversal/) — Easy

### SQL
- [LeetCode Database problem list](https://leetcode.com/problem-list/database/) — practice JOIN + GROUP BY against unfamiliar schemas

---

## Sources

- [Boyer-Moore Majority Voting Algorithm – GeeksforGeeks](https://www.geeksforgeeks.org/theory-of-computation/boyer-moore-majority-voting-algorithm/)
- [The Boyer-Moore Vote Algorithm Explained Simply – Medium](https://medium.com/@aibhi.dev/the-boyer-moore-vote-algorithm-explained-simply-8901686d7179)
- [Sweep Line Algorithm – Wikipedia](https://en.wikipedia.org/wiki/Sweep_line_algorithm)
- [Mastering the Line Sweep Algorithm for Interval Problems – Medium](https://coderraj07.medium.com/mastering-the-line-sweep-algorithm-for-interval-problems-298c4dc562aa)
- [Leetcode 253. Meeting Rooms II – Hello Interview](https://www.hellointerview.com/community/questions/meeting-rooms-2/cm5eh7nrh04u1838orn6jicpz)
- [Two Pointers Technique – GeeksforGeeks](https://www.geeksforgeeks.org/dsa/two-pointers-technique/)
- [DSA Fundamentals: Two Pointers & Sliding Window – Dev.to](https://dev.to/jayk0001/dsa-fundamentals-two-pointers-sliding-window-from-theory-to-leetcode-practice-80f)
- [Hash table cheatsheet for coding interviews – Tech Interview Handbook](https://www.techinterviewhandbook.org/algorithms/hash-table/)
- [Understanding Hash Tables and Their Applications in Interviews – AlgoCademy](https://algocademy.com/blog/understanding-hash-tables-and-their-applications-in-interviews/)
- [Kadane's Algorithm – Codecademy](https://www.codecademy.com/article/kadanes-algorithm-find-maximum-subarray-sum-in-an-array)
- [Understanding Kadane's Algorithm – Preplaced](https://www.preplaced.in/blog/understanding-kadanes-algorithm)
- [SQL Joins Explained: INNER, LEFT, RIGHT, FULL – DbSchema](https://dbschema.com/blog/tutorials/sql-joins-explained/)
- [SQL Joins (Inner, Left, Right and Full Join) – GeeksforGeeks](https://www.geeksforgeeks.org/sql/sql-join-set-1-inner-left-right-and-full-joins/)
- [Efficient palindrome checking algorithm explained – Medium](https://medium.com/@ishifoev/efficient-palindrome-checking-algorithm-explained-with-examples-12e0053e260f)
- [680. Valid Palindrome II – In-Depth Explanation – algo.monster](https://algo.monster/liteproblems/680)
- [Longest Palindromic Substring – NeetCode](https://neetcode.io/problems/longest-palindromic-substring/question)
- [Longest Palindromic Substring – GeeksforGeeks](https://www.geeksforgeeks.org/dsa/longest-palindromic-substring/)
