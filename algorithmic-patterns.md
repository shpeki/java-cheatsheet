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
