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

**Java Example (Two Sum Sorted):**
```java
public int[] twoSum(int[] numbers, int target) {
    int left = 0;
    int right = numbers.length - 1;
    
    while (left < right) {
        int sum = numbers[left] + numbers[right];
        
        if (sum == target) {
            return new int[] {left + 1, right + 1}; // 1-indexed result
        } else if (sum < target) {
            left++;
        } else {
            right--;
        }
    }
    
    return new int[] {-1, -1}; // No solution found
}
```

## 2. Fast & Slow Pointers (Tortoise and Hare)

**📌 Description:**

One pointer moves faster than the other to detect cycles or find middle points.

**🧠 Use Cases:**
* Detect cycle in a linked list
* Find the middle of a linked list
* Happy number problem
* Circular array loop detection

**Java Example (Detect Cycle in a Linked List):**
```java
public class ListNode {
    int val;
    ListNode next;
    ListNode(int x) { val = x; }
}

public boolean hasCycle(ListNode head) {
    if (head == null || head.next == null) {
        return false;
    }
    
    ListNode slow = head;
    ListNode fast = head;
    
    while (fast != null && fast.next != null) {
        slow = slow.next;          // Move slow pointer by 1
        fast = fast.next.next;     // Move fast pointer by 2
        
        if (slow == fast) {        // If they meet, there's a cycle
            return true;
        }
    }
    
    return false;                  // Fast pointer reached the end, so no cycle
}
```

## 3. Prefix Sum / Cumulative Sum

**📌 Description:**

Preprocess an array to store cumulative values for quick range calculations.

**📍 Similar to:**

Sliding window when you're summing over ranges but want faster access.

**🔧 Use Cases:**
* Subarray sum equals k
* Range sum queries (e.g., [L, R] sums)
* Number of subarrays with a given sum

**Java Example (Subarray Sum Equals K):**
```java
public int subarraySum(int[] nums, int k) {
    int count = 0;
    int sum = 0;
    
    // Map to store prefix sum frequencies
    HashMap<Integer, Integer> prefixSumCount = new HashMap<>();
    prefixSumCount.put(0, 1); // Empty subarray has sum 0
    
    for (int num : nums) {
        sum += num;
        
        // If (sum - k) exists in our map, we've found subarrays with sum k
        if (prefixSumCount.containsKey(sum - k)) {
            count += prefixSumCount.get(sum - k);
        }
        
        // Update the prefix sum frequency
        prefixSumCount.put(sum, prefixSumCount.getOrDefault(sum, 0) + 1);
    }
    
    return count;
}
```

## 4. Monotonic Queue / Deque

**📌 Description:**

A specialized structure to maintain increasing or decreasing order of elements in a sliding window.

**🔧 Use Cases:**
* Maximum/Minimum in every window of size k
* Sliding window median
* Histogram rectangle areas

**Java Example (Maximum in Sliding Window):**
```java
public int[] maxSlidingWindow(int[] nums, int k) {
    if (nums == null || nums.length == 0) {
        return new int[0];
    }
    
    int n = nums.length;
    int[] result = new int[n - k + 1];
    Deque<Integer> deque = new ArrayDeque<>(); // Stores indices
    
    for (int i = 0; i < n; i++) {
        // Remove elements outside the current window
        while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        
        // Remove smaller elements from the back
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        
        // Add current element index
        deque.offerLast(i);
        
        // Add the maximum to result when window is complete
        if (i >= k - 1) {
            result[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    
    return result;
}
```

## 5. Hash Map / Hash Set (for frequency/count tracking)

**📌 Description:**

Used with sliding windows to track character counts, uniqueness, or presence.

**🔧 Use Cases:**
* Longest substring without repeating characters
* Anagram detection in a window
* Minimum window substring

**Java Example (Longest Substring Without Repeating Characters):**
```java
public int lengthOfLongestSubstring(String s) {
    if (s == null || s.length() == 0) {
        return 0;
    }
    
    int maxLength = 0;
    int left = 0;
    
    // Use HashMap to store character -> its index
    HashMap<Character, Integer> charMap = new HashMap<>();
    
    for (int right = 0; right < s.length(); right++) {
        char currentChar = s.charAt(right);
        
        // If character is already in current window, move left pointer
        if (charMap.containsKey(currentChar)) {
            left = Math.max(left, charMap.get(currentChar) + 1);
        }
        
        // Update character position
        charMap.put(currentChar, right);
        
        // Update max length
        maxLength = Math.max(maxLength, right - left + 1);
    }
    
    return maxLength;
}
```

## 6. Backtracking (for combinations/permutations with constraints)

**📌 Description:**

A recursive pattern to build candidates and backtrack when a constraint is violated.

**🔧 Use Cases:**
* Subsets, permutations, combinations
* Word search
* N-Queens
* Sudoku solver

**Java Example (Generate Subsets):**
```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), nums, 0);
    return result;
}

private void backtrack(List<List<Integer>> result, List<Integer> tempList, int[] nums, int start) {
    // Add the current subset
    result.add(new ArrayList<>(tempList));
    
    // Try adding each remaining number
    for (int i = start; i < nums.length; i++) {
        // Add the number to our current subset
        tempList.add(nums[i]);
        
        // Recurse with the next position
        backtrack(result, tempList, nums, i + 1);
        
        // Backtrack - remove the number to try another one
        tempList.remove(tempList.size() - 1);
    }
}
```

## 7. Binary Search on Answer

**📌 Description:**

Used when the problem is "search for the smallest/largest X that satisfies Y."

**🔧 Use Cases:**
* Minimum capacity to ship packages
* Koko eating bananas
* Median of two sorted arrays
* Allocate books

**Java Example (Koko Eating Bananas):**
```java
public int minEatingSpeed(int[] piles, int h) {
    // The minimum eating speed is 1
    int left = 1;
    
    // The maximum eating speed is the largest pile
    int right = 1;
    for (int pile : piles) {
        right = Math.max(right, pile);
    }
    
    // Binary search to find the smallest valid eating speed
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (canFinish(piles, h, mid)) {
            // If Koko can finish with this speed, try a smaller one
            right = mid;
        } else {
            // If Koko can't finish, try a larger speed
            left = mid + 1;
        }
    }
    
    return left;
}

private boolean canFinish(int[] piles, int h, int speed) {
    int hoursNeeded = 0;
    
    for (int pile : piles) {
        // Math.ceil(pile / (double) speed)
        hoursNeeded += (pile + speed - 1) / speed;
        
        if (hoursNeeded > h) {
            return false;
        }
    }
    
    return true;
}
```

## 8. DFS/BFS on Graphs or Trees

**📌 Description:**

Graph traversal techniques that often use recursion (DFS) or queues (BFS).

**🔧 Use Cases:**
* Word ladder
* Shortest path
* Tree diameter, max depth
* Clone graph

**Java Example (BFS for Shortest Path):**
```java
public int shortestPath(int[][] grid, int[] start, int[] destination) {
    int rows = grid.length;
    int cols = grid[0].length;
    
    // Direction vectors for up, right, down, left
    int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
    
    // Queue for BFS
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{start[0], start[1]});
    
    // Mark start as visited
    boolean[][] visited = new boolean[rows][cols];
    visited[start[0]][start[1]] = true;
    
    int distance = 0;
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        
        // Process all nodes at current distance
        for (int i = 0; i < size; i++) {
            int[] current = queue.poll();
            
            // If destination is reached
            if (current[0] == destination[0] && current[1] == destination[1]) {
                return distance;
            }
            
            // Explore all four directions
            for (int[] dir : directions) {
                int newRow = current[0] + dir[0];
                int newCol = current[1] + dir[1];
                
                // Check if valid and not visited
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols 
                        && grid[newRow][newCol] == 0 && !visited[newRow][newCol]) {
                    queue.offer(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                }
            }
        }
        
        // Increment distance after processing the current level
        distance++;
    }
    
    return -1; // No path found
}
```


# Array Manipulation Problems for Java Interviews

This document covers common array-based algorithm problems that involve complex index manipulations, particularly those with patterns like `i-j-1`. These problems frequently appear in technical interviews for Java positions.

## Problems Involving i-j-1 Pattern and Similar Index Calculations

### 1. Trapping Rain Water

**Problem:** Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.

**Solution:**
```java
public int trap(int[] height) {
    int n = height.length;
    if (n <= 2) return 0;
    
    int[] leftMax = new int[n];
    int[] rightMax = new int[n];
    
    // Fill leftMax array
    leftMax[0] = height[0];
    for (int i = 1; i < n; i++) {
        leftMax[i] = Math.max(leftMax[i-1], height[i]);
    }
    
    // Fill rightMax array
    rightMax[n-1] = height[n-1];
    for (int i = n-2; i >= 0; i--) {
        rightMax[i] = Math.max(rightMax[i+1], height[i]);
    }
    
    // Calculate trapped water
    int trappedWater = 0;
    for (int i = 0; i < n; i++) {
        trappedWater += Math.min(leftMax[i], rightMax[i]) - height[i];
    }
    
    return trappedWater;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

### 2. Container With Most Water

**Problem:** Given n non-negative integers a₁, a₂, ..., aₙ, where each represents a point at coordinate (i, aᵢ). n vertical lines are drawn such that the two endpoints of the line i is at (i, aᵢ) and (i, 0). Find two lines, which, together with the x-axis forms a container, such that the container contains the most water.

**Solution:**
```java
public int maxArea(int[] height) {
    int maxWater = 0;
    int left = 0;
    int right = height.length - 1;
    
    while (left < right) {
        // Width is right - left (difference between indices)
        int width = right - left;
        int area = width * Math.min(height[left], height[right]);
        maxWater = Math.max(maxWater, area);
        
        // Move the pointer with smaller height
        if (height[left] < height[right]) {
            left++;
        } else {
            right--;
        }
    }
    
    return maxWater;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

### 3. Longest Valid Parentheses

**Problem:** Given a string containing just the characters '(' and ')', find the length of the longest valid (well-formed) parentheses substring.

**Solution:**
```java
public int longestValidParentheses(String s) {
    int maxLen = 0;
    Stack<Integer> stack = new Stack<>();
    stack.push(-1); // Base for length calculation
    
    for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == '(') {
            stack.push(i);
        } else { // ')'
            stack.pop();
            if (stack.isEmpty()) {
                // No matching opening bracket, push current as new base
                stack.push(i);
            } else {
                // Calculate length: current position - position before the valid substring
                // This is essentially (i - j - 1) + 1 = i - j where j is stack.peek()
                maxLen = Math.max(maxLen, i - stack.peek());
            }
        }
    }
    
    return maxLen;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

### 4. Minimum Size Subarray Sum

**Problem:** Given an array of positive integers nums and a positive integer target, return the minimal length of a subarray whose sum is greater than or equal to target. If there is no such subarray, return 0 instead.

**Solution:**
```java
public int minSubArrayLen(int target, int[] nums) {
    int n = nums.length;
    int left = 0;
    int sum = 0;
    int minLength = Integer.MAX_VALUE;
    
    for (int right = 0; right < n; right++) {
        sum += nums[right];
        
        // Shrink window from left when condition is met
        while (sum >= target) {
            // Update minimum length: (right - left + 1)
            minLength = Math.min(minLength, right - left + 1);
            sum -= nums[left++];
        }
    }
    
    return minLength == Integer.MAX_VALUE ? 0 : minLength;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

### 5. Maximum Product Subarray

**Problem:** Given an integer array nums, find a subarray that has the largest product, and return the product.

**Solution:**
```java
public int maxProduct(int[] nums) {
    if (nums == null || nums.length == 0) return 0;
    
    int maxSoFar = nums[0];
    int minEndingHere = nums[0]; // Track minimum because of negative numbers
    int maxEndingHere = nums[0];
    
    for (int i = 1; i < nums.length; i++) {
        int temp = maxEndingHere;
        
        // When multiplying by a negative number, min becomes max and max becomes min
        maxEndingHere = Math.max(Math.max(nums[i], maxEndingHere * nums[i]), minEndingHere * nums[i]);
        minEndingHere = Math.min(Math.min(nums[i], temp * nums[i]), minEndingHere * nums[i]);
        
        maxSoFar = Math.max(maxSoFar, maxEndingHere);
    }
    
    return maxSoFar;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

### 6. Subarrays with Sum K

**Problem:** Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.

**Solution:**
```java
public int subarraySum(int[] nums, int k) {
    HashMap<Integer, Integer> prefixSumCount = new HashMap<>();
    prefixSumCount.put(0, 1); // Empty subarray with sum 0
    
    int count = 0;
    int prefixSum = 0;
    
    for (int i = 0; i < nums.length; i++) {
        prefixSum += nums[i];
        
        // Check if there's a previous prefix sum that can be subtracted to get k
        // This creates a pattern where we're finding j such that sum(j+1...i) = k
        if (prefixSumCount.containsKey(prefixSum - k)) {
            count += prefixSumCount.get(prefixSum - k);
        }
        
        prefixSumCount.put(prefixSum, prefixSumCount.getOrDefault(prefixSum, 0) + 1);
    }
    
    return count;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

### 7. Longest Substring Without Repeating Characters

**Problem:** Given a string s, find the length of the longest substring without repeating characters.

**Solution:**
```java
public int lengthOfLongestSubstring(String s) {
    if (s == null || s.length() == 0) return 0;
    
    int maxLength = 0;
    int left = 0;
    HashMap<Character, Integer> charMap = new HashMap<>();
    
    for (int right = 0; right < s.length(); right++) {
        char currentChar = s.charAt(right);
        
        // If already in window, move left pointer to position after the duplicate
        if (charMap.containsKey(currentChar)) {
            // Math.max is necessary because the left pointer must always move forward
            left = Math.max(left, charMap.get(currentChar) + 1);
        }
        
        // Update character position
        charMap.put(currentChar, right);
        
        // Update max length: (right - left + 1) is the current window size
        maxLength = Math.max(maxLength, right - left + 1);
    }
    
    return maxLength;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(min(m, n)) where m is the size of the character set

## More Advanced Array Problems

### 8. Sliding Window Maximum

**Problem:** You are given an array of integers nums, a sliding window of size k moving from the very left to the very right. Return the maximum value in each window.

**Solution:**
```java
public int[] maxSlidingWindow(int[] nums, int k) {
    if (nums == null || nums.length == 0 || k <= 0) {
        return new int[0];
    }
    
    int n = nums.length;
    int[] result = new int[n - k + 1];
    Deque<Integer> deque = new ArrayDeque<>(); // Store indices
    
    for (int i = 0; i < n; i++) {
        // Remove elements outside current window (i-k+1 is the start of window)
        while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        
        // Remove smaller elements (they'll never be the max)
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        
        deque.offerLast(i);
        
        // Start adding to result when we have a full window
        if (i >= k - 1) {
            result[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    
    return result;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(k)

### 9. Next Permutation

**Problem:** Implement next permutation, which rearranges numbers into the lexicographically next greater permutation of numbers. If such an arrangement is not possible, it must rearrange it as the lowest possible order (i.e., sorted in ascending order).

**Solution:**
```java
public void nextPermutation(int[] nums) {
    int n = nums.length;
    int i = n - 2;
    
    // Find the first decreasing element from the end
    while (i >= 0 && nums[i] >= nums[i + 1]) {
        i--;
    }
    
    if (i >= 0) {
        int j = n - 1;
        // Find the element just larger than nums[i]
        while (nums[j] <= nums[i]) {
            j--;
        }
        swap(nums, i, j);
    }
    
    // Reverse the subarray after position i
    reverse(nums, i + 1, n - 1);
}

private void swap(int[] nums, int i, int j) {
    int temp = nums[i];
    nums[i] = nums[j];
    nums[j] = temp;
}

private void reverse(int[] nums, int start, int end) {
    while (start < end) {
        swap(nums, start++, end--);
    }
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

### 10. Merge Intervals

**Problem:** Given an array of intervals where intervals[i] = [starti, endi], merge all overlapping intervals, and return an array of the non-overlapping intervals that cover all the intervals in the input.

**Solution:**
```java
public int[][] merge(int[][] intervals) {
    if (intervals == null || intervals.length <= 1) {
        return intervals;
    }
    
    // Sort by start time
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    
    List<int[]> result = new ArrayList<>();
    int[] current = intervals[0];
    result.add(current);
    
    for (int i = 1; i < intervals.length; i++) {
        // Current interval's end is >= next interval's start -> overlap
        if (current[1] >= intervals[i][0]) {
            // Extend current interval's end if needed
            current[1] = Math.max(current[1], intervals[i][1]);
        } else {
            // No overlap, add this as a new interval
            current = intervals[i];
            result.add(current);
        }
    }
    
    return result.toArray(new int[result.size()][]);
}
```

**Time Complexity:** O(n log n) due to sorting  
**Space Complexity:** O(n)

### 11. Jump Game

**Problem:** You are given an integer array nums. You are initially positioned at the array's first index, and each element in the array represents your maximum jump length at that position. Return true if you can reach the last index, or false otherwise.

**Solution:**
```java
public boolean canJump(int[] nums) {
    int maxReachable = 0;
    
    for (int i = 0; i < nums.length; i++) {
        // If current position is beyond what we can reach, return false
        if (i > maxReachable) {
            return false;
        }
        
        // Update the furthest position we can reach
        maxReachable = Math.max(maxReachable, i + nums[i]);
        
        // If we can already reach the last index, return true
        if (maxReachable >= nums.length - 1) {
            return true;
        }
    }
    
    return true;
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

### 12. Maximum Sum Circular Subarray

**Problem:** Given a circular integer array nums of length n, return the maximum possible sum of a non-empty subarray of nums. A circular array means the end of the array connects to the beginning.

**Solution:**
```java
public int maxSubarraySumCircular(int[] nums) {
    if (nums == null || nums.length == 0) return 0;
    
    int totalSum = 0;
    int maxStraightSum = Integer.MIN_VALUE;
    int minStraightSum = Integer.MAX_VALUE;
    int currentMax = 0;
    int currentMin = 0;
    
    for (int num : nums) {
        totalSum += num;
        
        // For maximum subarray sum
        currentMax = Math.max(currentMax + num, num);
        maxStraightSum = Math.max(maxStraightSum, currentMax);
        
        // For minimum subarray sum
        currentMin = Math.min(currentMin + num, num);
        minStraightSum = Math.min(minStraightSum, currentMin);
    }
    
    // If all numbers are negative, return the maximum (least negative)
    if (maxStraightSum < 0) {
        return maxStraightSum;
    }
    
    // Either the max straight sum or the total sum minus the min straight sum (circular case)
    return Math.max(maxStraightSum, totalSum - minStraightSum);
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(1)

## Interview Tips for Array Problems

1. **Two Pointer Technique:** Often a good approach when dealing with array and string problems. Look for opportunities to use this technique when the problem involves searching for a pair, triplet, or subsequence.

2. **Sliding Window:** Especially useful for substring or subarray problems with a constraint.

3. **Prefix Sums:** Great for range sum queries and finding subarrays with a particular sum.

4. **Binary Search:** Can be applied to sorted arrays to reduce time complexity, but also for "search for an answer" problems.

5. **Time and Space Complexity:** Always analyze your solution's efficiency. Interviewers often expect optimal solutions.

6. **Edge Cases:** Consider empty arrays, single-element arrays, and arrays with negative numbers.

7. **Sorting:** Sometimes sorting the array first simplifies the problem significantly.

8. **Monotonic Stack/Queue:** Useful for finding the next greater/smaller element or problems like the histogram area.

Remember that many array problems can be solved using multiple approaches. Practice explaining your thinking process and consider trade-offs between different solutions.


# Solving Palindrome Problems in Java

Palindrome problems are a common type of string/array manipulation task in coding interviews. A palindrome is a sequence that reads the same forward and backward (like "racecar" or "madam").

This document covers the most common palindrome problems and their Java solutions.

## 1. Check if a String is a Palindrome

The most basic palindrome problem is to check if a given string is a palindrome.

### Two-Pointer Approach

```java
public boolean isPalindrome(String s) {
    // Clean the string: convert to lowercase and remove non-alphanumeric characters
    s = s.toLowerCase().replaceAll("[^a-z0-9]", "");
    
    int left = 0;
    int right = s.length() - 1;
    
    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) {
            return false;
        }
        left++;
        right--;
    }
    
    return true;
}
```

**Time Complexity**: O(n)  
**Space Complexity**: O(1) (not counting the cleaned string)

## 2. Longest Palindromic Substring

Find the longest substring that is a palindrome in a given string.

### Expand Around Center Approach

```java
public String longestPalindrome(String s) {
    if (s == null || s.length() < 1) return "";
    
    int start = 0, end = 0;
    
    for (int i = 0; i < s.length(); i++) {
        // Handle odd-length palindromes (centered at i)
        int len1 = expandAroundCenter(s, i, i);
        
        // Handle even-length palindromes (centered between i and i+1)
        int len2 = expandAroundCenter(s, i, i + 1);
        
        int len = Math.max(len1, len2);
        
        // Update longest palindrome if current is longer
        if (len > end - start) {
            // Calculate start and end indices
            start = i - (len - 1) / 2;
            end = i + len / 2;
        }
    }
    
    return s.substring(start, end + 1);
}

private int expandAroundCenter(String s, int left, int right) {
    while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
        left--;
        right++;
    }
    // Return length of palindrome
    // Note the i-j-1 pattern: right-left-1
    return right - left - 1;
}
```

**Time Complexity**: O(n²)  
**Space Complexity**: O(1)

### Why right-left-1?

In the `expandAroundCenter` method, we use `right - left - 1` to calculate the length of the palindrome. This is a classic example of the i-j-1 pattern in array/string problems. When the while loop exits, both `left` and `right` have gone one position too far (left is one position before the palindrome starts, and right is one position after it ends), so the length is `(right - 1) - (left + 1) + 1 = right - left - 1`.

## 3. Count All Palindromic Substrings

Count the number of palindromic substrings in a given string.

```java
public int countSubstrings(String s) {
    int count = 0;
    
    for (int i = 0; i < s.length(); i++) {
        // Count odd-length palindromes centered at i
        count += countPalindromesAroundCenter(s, i, i);
        
        // Count even-length palindromes centered between i and i+1
        count += countPalindromesAroundCenter(s, i, i + 1);
    }
    
    return count;
}

private int countPalindromesAroundCenter(String s, int left, int right) {
    int count = 0;
    
    while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
        count++;
        left--;
        right++;
    }
    
    return count;
}
```

**Time Complexity**: O(n²)  
**Space Complexity**: O(1)

## 4. Dynamic Programming Approach for Palindromes

For some palindrome problems, dynamic programming provides an efficient solution by avoiding redundant calculations.

```java
public String longestPalindromeDP(String s) {
    int n = s.length();
    if (n < 2) return s;
    
    // dp[i][j] = true if substring s[i...j] is palindrome
    boolean[][] dp = new boolean[n][n];
    
    // All substrings of length 1 are palindromes
    for (int i = 0; i < n; i++) {
        dp[i][i] = true;
    }
    
    int start = 0;
    int maxLength = 1;
    
    // Check for substrings of length 2
    for (int i = 0; i < n - 1; i++) {
        if (s.charAt(i) == s.charAt(i + 1)) {
            dp[i][i + 1] = true;
            start = i;
            maxLength = 2;
        }
    }
    
    // Check for lengths greater than 2
    // k is the substring length - 1
    for (int k = 3; k <= n; k++) {
        // i is the starting index
        for (int i = 0; i < n - k + 1; i++) {
            // j is the ending index
            int j = i + k - 1;
            
            // Check if current substring is palindrome
            if (dp[i + 1][j - 1] && s.charAt(i) == s.charAt(j)) {
                dp[i][j] = true;
                
                if (k > maxLength) {
                    start = i;
                    maxLength = k;
                }
            }
        }
    }
    
    return s.substring(start, start + maxLength);
}
```

**Time Complexity**: O(n²)  
**Space Complexity**: O(n²)

## 5. Palindrome Partitioning

Find all possible ways to partition a string so that each substring is a palindrome.

```java
public List<List<String>> partition(String s) {
    List<List<String>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), s, 0);
    return result;
}

private void backtrack(List<List<String>> result, List<String> current, String s, int start) {
    if (start >= s.length()) {
        result.add(new ArrayList<>(current));
        return;
    }
    
    for (int end = start; end < s.length(); end++) {
        if (isPalindrome(s, start, end)) {
            // Add current palindrome substring
            current.add(s.substring(start, end + 1));
            // Recurse for the remaining string
            backtrack(result, current, s, end + 1);
            // Backtrack
            current.remove(current.size() - 1);
        }
    }
}

private boolean isPalindrome(String s, int low, int high) {
    while (low < high) {
        if (s.charAt(low++) != s.charAt(high--)) {
            return false;
        }
    }
    return true;
}
```

**Time Complexity**: O(n * 2^n) in the worst case  
**Space Complexity**: O(n) for recursion stack

## 6. Valid Palindrome II (Allow One Deletion)

Determine if a string can become a palindrome by deleting at most one character.

```java
public boolean validPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;
    
    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) {
            // Try skipping character at left or right
            return isPalindrome(s, left + 1, right) || isPalindrome(s, left, right - 1);
        }
        left++;
        right--;
    }
    
    return true;
}

private boolean isPalindrome(String s, int left, int right) {
    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) {
            return false;
        }
        left++;
        right--;
    }
    return true;
}
```

**Time Complexity**: O(n)  
**Space Complexity**: O(1)

## 7. Palindromic Substrings in Arrays

The palindrome concept can also be applied to arrays, where we check if an array segment reads the same forward and backward.

```java
public boolean isArrayPalindrome(int[] arr, int start, int end) {
    while (start < end) {
        if (arr[start] != arr[end]) {
            return false;
        }
        start++;
        end--;
    }
    return true;
}
```

## 8. Manacher's Algorithm - Advanced Technique

For finding all palindromic substrings in a string, Manacher's algorithm provides an O(n) solution, but it's more complex:

```java
public String longestPalindromeManacher(String s) {
    if (s == null || s.length() == 0) return "";
    
    // Preprocess the string to handle both odd and even length palindromes
    StringBuilder sb = new StringBuilder();
    sb.append('#');
    for (char c : s.toCharArray()) {
        sb.append(c);
        sb.append('#');
    }
    String processedString = sb.toString();
    
    int n = processedString.length();
    int[] p = new int[n]; // p[i] = radius of palindrome centered at i
    int center = 0;
    int rightBoundary = 0;
    
    for (int i = 0; i < n; i++) {
        // Mirror of i with respect to center
        int mirror = 2 * center - i;
        
        // If i is within the right boundary, we can use the mirror's value
        if (i < rightBoundary) {
            p[i] = Math.min(rightBoundary - i, p[mirror]);
        }
        
        // Attempt to expand palindrome centered at i
        int left = i - (p[i] + 1);
        int right = i + (p[i] + 1);
        
        while (left >= 0 && right < n && 
               processedString.charAt(left) == processedString.charAt(right)) {
            p[i]++;
            left--;
            right++;
        }
        
        // If palindrome centered at i expands past rightBoundary,
        // adjust center and boundary
        if (i + p[i] > rightBoundary) {
            center = i;
            rightBoundary = i + p[i];
        }
    }
    
    // Find the maximum element in p
    int maxLen = 0;
    int centerIndex = 0;
    for (int i = 0; i < n; i++) {
        if (p[i] > maxLen) {
            maxLen = p[i];
            centerIndex = i;
        }
    }
    
    // Extract the longest palindrome
    int start = (centerIndex - maxLen) / 2;
    return s.substring(start, start + maxLen);
}
```

**Time Complexity**: O(n)  
**Space Complexity**: O(n)

## Interview Tips for Palindrome Problems

1. **Two-Pointer Technique**: This is the most efficient way to check if a string is a palindrome - start from both ends and move inward.

2. **Expand Around Center**: For finding palindromic substrings, the expand-around-center approach is intuitive and efficient.

3. **Handle Both Odd and Even Length**: Remember that palindromes can have odd or even lengths. For odd lengths, expand from a single character. For even lengths, expand from between two characters.

4. **String Preprocessing**: For case-insensitive palindrome checks, convert to lowercase and remove non-alphanumeric characters first.

5. **Dynamic Programming**: While more complex, DP can be useful for certain palindrome problems, especially when you need to store intermediate results.

6. **Consider Edge Cases**: Empty strings, single characters, and strings with all identical characters are all palindromes.

7. **Optimization**: For very long strings, you can add early termination checks to improve average-case performance.

8. **Manacher's Algorithm**: Know about this O(n) algorithm for finding all palindromic substrings, but understand that it's complex and typically not expected in standard interviews unless specifically mentioned.

By understanding these common approaches and the i-j-1 pattern involved in many palindrome problems, you'll be well-prepared to solve most palindrome-related interview questions.

# Analyzing Time and Space Complexity for Coding Interviews

Understanding how to analyze time and space complexity is essential for coding interviews. This document provides a systematic approach to determining algorithmic complexity, with specific examples from array and string problems.

## Understanding Big O Notation

Big O notation is used to describe the upper bound of an algorithm's time or space requirements as a function of input size.

### Common Time Complexities (from fastest to slowest)

1. **O(1) - Constant Time**: The algorithm takes the same amount of time regardless of input size.
2. **O(log n) - Logarithmic Time**: Time increases logarithmically with input size.
3. **O(n) - Linear Time**: Time increases linearly with input size.
4. **O(n log n) - Linearithmic Time**: Time increases by n log n with input size.
5. **O(n²) - Quadratic Time**: Time increases quadratically with input size.
6. **O(2^n) - Exponential Time**: Time doubles with each additional input element.
7. **O(n!) - Factorial Time**: Time increases factorially with input size.

## How to Calculate Time Complexity

### Step 1: Identify the Basic Operations

Count the operations that dominate the runtime:
- Assignments, arithmetic operations, comparisons
- Array/string access or modification
- Function calls

### Step 2: Determine How Many Times Operations Are Executed

Count how many times each operation is performed based on the input size n.

### Step 3: Express the Count as a Function of the Input Size

Simplify the function by:
- Dropping constants
- Keeping only the highest-order term

## How to Calculate Space Complexity

Space complexity measures the additional memory an algorithm uses as a function of the input size.

### Step 1: Identify Memory Allocations

Count:
- Variables and constants
- Data structures
- Recursion stack

### Step 2: Determine How Memory Usage Scales with Input Size

Express memory usage as a function of n.

### Step 3: Simplify to the Highest-Order Term

Just like with time complexity, keep only the dominant term.

## Examples with Array and String Algorithms

### Example 1: Linear Search in an Array

```java
public int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;
        }
    }
    return -1;
}
```

**Time Complexity Analysis:**
- Basic operation: Comparison `arr[i] == target`
- How many times: In the worst case, n times (where n is array length)
- Function: O(n) - Linear time

**Space Complexity Analysis:**
- Additional memory: Only a few variables (i, target)
- Function: O(1) - Constant space

### Example 2: Two Sum Problem (Brute Force)

```java
public int[] twoSum(int[] nums, int target) {
    for (int i = 0; i < nums.length; i++) {
        for (int j = i + 1; j < nums.length; j++) {
            if (nums[i] + nums[j] == target) {
                return new int[] {i, j};
            }
        }
    }
    return new int[] {};
}
```

**Time Complexity Analysis:**
- Basic operation: Comparison `nums[i] + nums[j] == target`
- Outer loop: n iterations
- Inner loop: Up to n-1, n-2, ..., 1 iterations
- Total comparisons: n + (n-1) + (n-2) + ... + 1 = n(n+1)/2
- Simplified: O(n²) - Quadratic time

**Space Complexity Analysis:**
- Additional memory: Only a few variables
- Function: O(1) - Constant space

### Example 3: Two Sum Problem (Optimized with HashMap)

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[] {map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    
    return new int[] {};
}
```

**Time Complexity Analysis:**
- Basic operations: HashMap lookup and insertion (both typically O(1))
- How many times: n iterations through the array
- Function: O(n) - Linear time

**Space Complexity Analysis:**
- Additional memory: HashMap can store up to n elements
- Function: O(n) - Linear space

### Example 4: Merge Sort

```java
public void mergeSort(int[] arr, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }
}
```

**Time Complexity Analysis:**
- Recurrence relation: T(n) = 2T(n/2) + O(n)
- Using Master Theorem: O(n log n) - Linearithmic time

**Space Complexity Analysis:**
- Recursion depth: O(log n)
- Temporary arrays in merge: O(n)
- Function: O(n) - Linear space

### Example 5: Expanding Around Center for Palindromes

```java
public String longestPalindrome(String s) {
    if (s == null || s.length() < 1) return "";
    
    int start = 0, end = 0;
    
    for (int i = 0; i < s.length(); i++) {
        int len1 = expandAroundCenter(s, i, i);
        int len2 = expandAroundCenter(s, i, i + 1);
        int len = Math.max(len1, len2);
        
        if (len > end - start) {
            start = i - (len - 1) / 2;
            end = i + len / 2;
        }
    }
    
    return s.substring(start, end + 1);
}

private int expandAroundCenter(String s, int left, int right) {
    while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
        left--;
        right++;
    }
    return right - left - 1;
}
```

**Time Complexity Analysis:**
- Main function: Loops n times
- expandAroundCenter: Each call can examine up to n characters
- Total operations: O(n²) - Quadratic time

**Space Complexity Analysis:**
- No additional data structures that scale with input size
- Function: O(1) - Constant space

## Common Patterns to Recognize

### 1. Loop-based complexity

#### Single loop through data
```java
for (int i = 0; i < n; i++) {
    // O(1) operations
}
```
**Time Complexity**: O(n)

#### Nested loops
```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        // O(1) operations
    }
}
```
**Time Complexity**: O(n²)

#### Logarithmic loops
```java
for (int i = 1; i < n; i = i * 2) {
    // O(1) operations
}
```
**Time Complexity**: O(log n)

### 2. Recursive algorithm complexity

For recursive functions, the time complexity often depends on:
- Number of recursive calls
- Work done in each call

**Example: Binary search**
```java
public int binarySearch(int[] arr, int target, int left, int right) {
    if (left > right) return -1;
    
    int mid = left + (right - left) / 2;
    
    if (arr[mid] == target) return mid;
    if (arr[mid] > target) return binarySearch(arr, target, left, mid - 1);
    return binarySearch(arr, target, mid + 1, right);
}
```
**Time Complexity**: O(log n) - Each call cuts the problem size in half

### 3. Space complexity in recursion

The space complexity of a recursive algorithm includes the recursion stack.

**Example: Factorial calculation**
```java
public int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}
```
**Space Complexity**: O(n) - Due to n recursive calls on the stack

## Tips for Time and Space Complexity Analysis in Interviews

1. **Analyze from highest to lowest complexity**: Start with the most expensive operations and see if they dominate the runtime.

2. **Consider average vs. worst case**: Be explicit about which case you're analyzing. For example, hash table lookups are O(1) on average but O(n) in the worst case.

3. **Watch for amortized complexity**: Some operations might be expensive occasionally but cheap on average (like resizing an ArrayList).

4. **Beware of hidden operations**: String concatenation, `ArrayList.add()`, and similar operations may have non-obvious complexity.

5. **For recursive algorithms**:
   - Identify the recurrence relation
   - Consider using the Master Theorem when applicable

6. **Explain your reasoning**: When analyzing complexity in an interview, explain your thought process step by step.

7. **For space complexity, remember**:
   - Input space vs. auxiliary space
   - Recursion stack usage
   - Temporary data structures

## Common Complexity Classes by Algorithm Type

| Algorithm Type | Typical Time Complexity | Examples |
|----------------|-------------------------|----------|
| Simple traversal | O(n) | Linear search, traversing a linked list |
| Binary search | O(log n) | Binary search in sorted array |
| Efficient sorting | O(n log n) | Merge sort, heap sort, quicksort (average) |
| Nested loops | O(n²) | Bubble sort, insertion sort, quadratic solutions |
| Hash-based | O(n) | Two Sum optimized, most hash table operations |
| Dynamic programming | O(n²) or O(n*m) | Longest common subsequence, knapsack problems |
| Backtracking | O(2^n) or O(n!) | N-Queens, permutations |

By understanding these patterns and practicing complexity analysis on diverse examples, you'll develop the intuition needed to quickly determine algorithmic efficiency during coding interviews.
