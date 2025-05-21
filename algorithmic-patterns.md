# Common Coding Patterns & Techniques

## 1. Two Pointers

**📌 Description:**

Use two pointers (often left and right) to iterate through a structure, usually from either end or in the same direction.

**📍 Similar to:**

Sliding window — often used to grow/shrink the window, or scan a sorted array.

**🔧 Use Cases:**
* Sorted array pair sum (twoSumSorted)
* Move zeros to the end
* Reverse a string or linked list
* Detect palindrome

## 2. Fast & Slow Pointers (Tortoise and Hare)

**📌 Description:**

One pointer moves faster than the other to detect cycles or find middle points.

**🧠 Use Cases:**
* Detect cycle in a linked list
* Find the middle of a linked list
* Happy number problem
* Circular array loop detection

## 3. Prefix Sum / Cumulative Sum

**📌 Description:**

Preprocess an array to store cumulative values for quick range calculations.

**📍 Similar to:**

Sliding window when you're summing over ranges but want faster access.

**🔧 Use Cases:**
* Subarray sum equals k
* Range sum queries (e.g., [L, R] sums)
* Number of subarrays with a given sum

## 4. Monotonic Queue / Deque

**📌 Description:**

A specialized structure to maintain increasing or decreasing order of elements in a sliding window.

**🔧 Use Cases:**
* Maximum/Minimum in every window of size k
* Sliding window median
* Histogram rectangle areas

## 5. Hash Map / Hash Set (for frequency/count tracking)

**📌 Description:**

Used with sliding windows to track character counts, uniqueness, or presence.

**🔧 Use Cases:**
* Longest substring without repeating characters
* Anagram detection in a window
* Minimum window substring

## 6. Backtracking (for combinations/permutations with constraints)

**📌 Description:**

A recursive pattern to build candidates and backtrack when a constraint is violated.

**🔧 Use Cases:**
* Subsets, permutations, combinations
* Word search
* N-Queens
* Sudoku solver

## 7. Binary Search on Answer

**📌 Description:**

Used when the problem is "search for the smallest/largest X that satisfies Y."

**🔧 Use Cases:**
* Minimum capacity to ship packages
* Koko eating bananas
* Median of two sorted arrays
* Allocate books

## 8. DFS/BFS on Graphs or Trees

**📌 Description:**

Graph traversal techniques that often use recursion (DFS) or queues (BFS).

**🔧 Use Cases:**
* Word ladder
* Shortest path
* Tree diameter, max depth
* Clone graph
