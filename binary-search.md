# Binary Search: Complete Guide with Java Examples

Binary search is one of the most fundamental and efficient searching algorithms in computer science. It's a cornerstone algorithm that every programmer should master, especially for coding interviews.

## What is Binary Search?

Binary search is a searching algorithm that finds the position of a target value within a **sorted array**. It works by repeatedly dividing the search interval in half and comparing the target with the middle element.

### Key Requirements:
1. **The array must be sorted** (ascending or descending)
2. **Random access** to elements (arrays work perfectly)

### Why is it called "Binary"?
Because at each step, it divides the search space into **two parts** (binary = two), eliminating half of the remaining elements.

## How Binary Search Works

### Basic Algorithm Steps:
1. Start with the entire array (left = 0, right = array.length - 1)
2. Find the middle element: `mid = left + (right - left) / 2`
3. Compare the middle element with the target:
   - If `arr[mid] == target`: Found! Return the index
   - If `arr[mid] < target`: Search the right half (left = mid + 1)
   - If `arr[mid] > target`: Search the left half (right = mid - 1)
4. Repeat until found or search space is empty

## Visual Example

Let's search for the number **7** in the array `[1, 3, 5, 7, 9, 11, 13]`:

```
Initial: [1, 3, 5, 7, 9, 11, 13]
         left=0      mid=3      right=6
         
Step 1: arr[3] = 7, target = 7
        FOUND at index 3!
```

Let's search for the number **5** in the same array:

```
Initial: [1, 3, 5, 7, 9, 11, 13]
         left=0      mid=3      right=6
         
Step 1: arr[3] = 7 > 5 (target)
        Search left half: [1, 3, 5, 7, 9, 11, 13]
                          left=0  mid=1  right=2
        
Step 2: arr[1] = 3 < 5 (target)  
        Search right half: [1, 3, 5, 7, 9, 11, 13]
                              left=2  mid=2  right=2
        
Step 3: arr[2] = 5 == 5 (target)
        FOUND at index 2!
```

## Java Implementation

### 1. Iterative Binary Search

```java
public int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;
    
    while (left <= right) {
        // Calculate mid to avoid integer overflow
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            return mid; // Found the target
        }
        
        if (arr[mid] < target) {
            left = mid + 1; // Search right half
        } else {
            right = mid - 1; // Search left half
        }
    }
    
    return -1; // Target not found
}
```

### 2. Recursive Binary Search

```java
public int binarySearchRecursive(int[] arr, int target) {
    return binarySearchHelper(arr, target, 0, arr.length - 1);
}

private int binarySearchHelper(int[] arr, int target, int left, int right) {
    // Base case: search space is empty
    if (left > right) {
        return -1;
    }
    
    int mid = left + (right - left) / 2;
    
    if (arr[mid] == target) {
        return mid;
    }
    
    if (arr[mid] < target) {
        // Search right half
        return binarySearchHelper(arr, target, mid + 1, right);
    } else {
        // Search left half
        return binarySearchHelper(arr, target, left, mid - 1);
    }
}
```

## Detailed Step-by-Step Example

Let's trace through searching for **9** in `[1, 3, 5, 7, 9, 11, 13, 15]`:

```java
// Array: [1, 3, 5, 7, 9, 11, 13, 15] (indices 0-7)
// Target: 9

// Step 1:
left = 0, right = 7
mid = 0 + (7-0)/2 = 3
arr[3] = 7
7 < 9, so search right half
left = mid + 1 = 4

// Step 2:
left = 4, right = 7  
mid = 4 + (7-4)/2 = 5
arr[5] = 11
11 > 9, so search left half
right = mid - 1 = 4

// Step 3:
left = 4, right = 4
mid = 4 + (4-4)/2 = 4
arr[4] = 9
9 == 9, FOUND at index 4!
```

## Common Variations of Binary Search

### 1. Find First Occurrence (Lower Bound)

When there are duplicate elements, find the first occurrence:

```java
public int findFirst(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;
    int result = -1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            result = mid;          // Store potential result
            right = mid - 1;       // Continue searching left
        } else if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    
    return result;
}
```

### 2. Find Last Occurrence (Upper Bound)

Find the last occurrence of the target:

```java
public int findLast(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;
    int result = -1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            result = mid;          // Store potential result
            left = mid + 1;        // Continue searching right
        } else if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    
    return result;
}
```

### 3. Find Insert Position

Find the position where target should be inserted to maintain sorted order:

```java
public int searchInsert(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        
        if (nums[mid] == target) {
            return mid;
        }
        
        if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    
    // If not found, left is the insertion position
    return left;
}
```

## Binary Search on Answer (Advanced Pattern)

Binary search can also be used to find answers in problems where we need to find the minimum or maximum value that satisfies a condition.

### Example: Square Root

```java
public int mySqrt(int x) {
    if (x < 2) return x;
    
    int left = 1;
    int right = x / 2;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        long square = (long) mid * mid; // Use long to avoid overflow
        
        if (square == x) {
            return mid;
        }
        
        if (square < x) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    
    return right; // right is the floor of the square root
}
```

### Example: Find Peak Element

```java
public int findPeakElement(int[] nums) {
    int left = 0;
    int right = nums.length - 1;
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (nums[mid] > nums[mid + 1]) {
            // Peak is in the left half (including mid)
            right = mid;
        } else {
            // Peak is in the right half
            left = mid + 1;
        }
    }
    
    return left;
}
```

## Time and Space Complexity

### Time Complexity: O(log n)
- Each comparison eliminates half of the remaining elements
- Maximum number of comparisons: ceil(log₂(n))
- For n = 1,000,000 elements, only about 20 comparisons needed!

### Space Complexity:
- **Iterative version**: O(1) - constant extra space
- **Recursive version**: O(log n) - due to recursion stack

## Common Pitfalls and How to Avoid Them

### 1. Integer Overflow in Mid Calculation

❌ **Wrong:**
```java
int mid = (left + right) / 2; // Can overflow if left + right > Integer.MAX_VALUE
```

✅ **Correct:**
```java
int mid = left + (right - left) / 2; // Safe from overflow
```

### 2. Infinite Loop with Wrong Boundary Updates

❌ **Wrong:**
```java
if (arr[mid] < target) {
    left = mid; // Should be mid + 1
}
```

✅ **Correct:**
```java
if (arr[mid] < target) {
    left = mid + 1;
}
```

### 3. Off-by-One Errors in Loop Condition

Make sure your loop condition and boundary updates are consistent:
- If using `left <= right`, update with `mid + 1` and `mid - 1`
- If using `left < right`, be careful with boundary updates

## Interview Tips for Binary Search

1. **Always clarify if the array is sorted** - Binary search only works on sorted data

2. **Ask about duplicates** - This affects which variation of binary search to use

3. **Handle edge cases**:
   - Empty array
   - Single element array
   - Target not in array
   - Target at boundaries

4. **Trace through a small example** - Show your understanding with a concrete example

5. **Explain the invariant** - "At each step, if the target exists, it must be in the range [left, right]"

6. **Mention the complexity** - O(log n) time, O(1) space for iterative version

## When to Use Binary Search

Binary search is applicable when:
- Data is sorted
- You need to search frequently
- You want O(log n) search time
- You're looking for a specific value or boundary

### Common Problem Types:
- Finding elements in sorted arrays
- Finding insert positions
- Search in rotated sorted arrays
- Finding square roots or other mathematical functions
- Capacity/resource allocation problems
- Finding peaks or valleys

## Practice Problems

Here are some LeetCode problems to practice binary search:

1. **Binary Search** (704) - Basic implementation
2. **Search Insert Position** (35) - Finding insert position
3. **First Bad Version** (278) - Binary search on answer
4. **Search in Rotated Sorted Array** (33) - Modified binary search
5. **Find Peak Element** (162) - Peak finding
6. **Sqrt(x)** (69) - Mathematical binary search
7. **Search a 2D Matrix** (74) - 2D binary search

Binary search is a powerful and elegant algorithm that demonstrates the beauty of divide-and-conquer approaches. Master it well, as it forms the foundation for many other advanced algorithms and problem-solving techniques!
