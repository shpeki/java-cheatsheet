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
