# SQL SELECT Basics for Product Owners

## Introduction

As a product owner, understanding SQL SELECT statements helps you communicate effectively with engineering teams and make data-driven decisions. This guide covers the essential SELECT operations you'll likely encounter in interviews.

## Sample Database

For all examples, we'll use a simple e-commerce database with these tables:

**users**
| user_id | name | email | registration_date | country |
|---------|------|-------|------------------|---------|
| 1 | John Smith | john@email.com | 2024-01-15 | USA |
| 2 | Maria Garcia | maria@email.com | 2024-02-20 | Spain |
| 3 | Chen Wei | chen@email.com | 2024-01-10 | China |
| 4 | Sarah Johnson | sarah@email.com | 2024-03-05 | USA |

**orders**
| order_id | user_id | total_amount | order_date | status |
|----------|---------|--------------|------------|--------|
| 101 | 1 | 150.00 | 2024-01-20 | completed |
| 102 | 2 | 75.50 | 2024-02-25 | completed |
| 103 | 1 | 200.00 | 2024-03-01 | completed |
| 104 | 3 | 50.00 | 2024-03-10 | pending |
| 105 | 4 | 300.00 | 2024-03-15 | completed |

## 1. Basic SELECT Statement

The most fundamental SQL operation - retrieving data from a table.

```sql
-- Get all columns from users table
SELECT * FROM users;

-- Get specific columns
SELECT name, email FROM users;

-- Get users from a specific country
SELECT name, email 
FROM users 
WHERE country = 'USA';
```

**Business Use Case:** "Show me all users who registered from the USA so we can analyze our American market penetration."

## 2. SELECT with Filtering (WHERE)

Filter data based on specific conditions.

```sql
-- Orders above $100
SELECT order_id, user_id, total_amount 
FROM orders 
WHERE total_amount > 100;

-- Orders from specific date range
SELECT * 
FROM orders 
WHERE order_date BETWEEN '2024-02-01' AND '2024-02-28';

-- Multiple conditions
SELECT name, email 
FROM users 
WHERE country = 'USA' AND registration_date > '2024-02-01';
```

**Business Use Case:** "Find all high-value orders (>$100) from February to understand our premium customer behavior."

## 3. SELECT with Sorting (ORDER BY)

Sort results in ascending or descending order.

```sql
-- Sort users by registration date (newest first)
SELECT name, registration_date 
FROM users 
ORDER BY registration_date DESC;

-- Sort orders by amount (highest first)
SELECT order_id, user_id, total_amount 
FROM orders 
ORDER BY total_amount DESC;

-- Multiple sorting criteria
SELECT name, country, registration_date 
FROM users 
ORDER BY country, registration_date DESC;
```

**Business Use Case:** "Show me our newest registered users first to track recent growth trends."

## 4. Aggregate Functions

Perform calculations on groups of data.

```sql
-- Count total number of users
SELECT COUNT(*) as total_users FROM users;

-- Sum of all order amounts
SELECT SUM(total_amount) as total_revenue FROM orders;

-- Average order value
SELECT AVG(total_amount) as average_order_value FROM orders;

-- Minimum and maximum order amounts
SELECT 
    MIN(total_amount) as smallest_order,
    MAX(total_amount) as largest_order
FROM orders;
```

**Business Use Case:** "What's our total revenue and average order value to measure business performance?"

## 5. GROUP BY - The Game Changer

Group data by specific columns and apply aggregate functions to each group. This is crucial for business analytics.

```sql
-- Number of users by country
SELECT country, COUNT(*) as user_count 
FROM users 
GROUP BY country;

-- Total revenue by user
SELECT user_id, SUM(total_amount) as total_spent 
FROM orders 
GROUP BY user_id;

-- Average order value by user
SELECT user_id, AVG(total_amount) as avg_order_value 
FROM orders 
GROUP BY user_id;

-- Order count and total revenue by month
SELECT 
    DATE_FORMAT(order_date, '%Y-%m') as month,
    COUNT(*) as order_count,
    SUM(total_amount) as monthly_revenue
FROM orders 
GROUP BY DATE_FORMAT(order_date, '%Y-%m');
```

**Business Use Case:** "Group our users by country to understand our geographic distribution and plan localization efforts."

## 6. HAVING - Filtering Groups

Filter groups after they've been created (unlike WHERE which filters individual rows).

```sql
-- Countries with more than 1 user
SELECT country, COUNT(*) as user_count 
FROM users 
GROUP BY country 
HAVING COUNT(*) > 1;

-- Users who spent more than $200 total
SELECT user_id, SUM(total_amount) as total_spent 
FROM orders 
GROUP BY user_id 
HAVING SUM(total_amount) > 200;

-- Months with revenue above $300
SELECT 
    DATE_FORMAT(order_date, '%Y-%m') as month,
    SUM(total_amount) as monthly_revenue
FROM orders 
GROUP BY DATE_FORMAT(order_date, '%Y-%m')
HAVING SUM(total_amount) > 300;
```

**Business Use Case:** "Show me only our high-value customers (total spent > $200) to create a VIP program."

## 7. Combining Everything - Complex Queries

Real-world scenarios often require combining multiple concepts.

```sql
-- Top spending customers from USA, ordered by total spent
SELECT 
    u.name,
    u.email,
    SUM(o.total_amount) as total_spent,
    COUNT(o.order_id) as order_count
FROM users u
JOIN orders o ON u.user_id = o.user_id
WHERE u.country = 'USA' AND o.status = 'completed'
GROUP BY u.user_id, u.name, u.email
HAVING SUM(o.total_amount) > 100
ORDER BY total_spent DESC;

-- Monthly revenue trend for completed orders only
SELECT 
    DATE_FORMAT(order_date, '%Y-%m') as month,
    COUNT(*) as completed_orders,
    SUM(total_amount) as revenue,
    AVG(total_amount) as avg_order_value
FROM orders 
WHERE status = 'completed'
GROUP BY DATE_FORMAT(order_date, '%Y-%m')
ORDER BY month;
```

## 8. Common Product Owner Questions You Can Answer

### Customer Segmentation
```sql
-- Segment customers by spending level
SELECT 
    CASE 
        WHEN total_spent >= 300 THEN 'High Value'
        WHEN total_spent >= 100 THEN 'Medium Value'
        ELSE 'Low Value'
    END as customer_segment,
    COUNT(*) as customer_count
FROM (
    SELECT user_id, SUM(total_amount) as total_spent
    FROM orders
    GROUP BY user_id
) customer_totals
GROUP BY customer_segment;
```

### Growth Analysis
```sql
-- Monthly user registration trend
SELECT 
    DATE_FORMAT(registration_date, '%Y-%m') as month,
    COUNT(*) as new_users
FROM users
GROUP BY DATE_FORMAT(registration_date, '%Y-%m')
ORDER BY month;
```

### Performance Metrics
```sql
-- Key business metrics summary
SELECT 
    COUNT(DISTINCT u.user_id) as total_users,
    COUNT(DISTINCT o.order_id) as total_orders,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value,
    SUM(o.total_amount) / COUNT(DISTINCT u.user_id) as revenue_per_user
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
WHERE o.status = 'completed';
```

## Key Takeaways for Product Owners

1. **SELECT** gets your data
2. **WHERE** filters individual records
3. **GROUP BY** organizes data into meaningful business segments
4. **HAVING** filters those segments based on aggregate conditions
5. **ORDER BY** sorts results for better presentation

Understanding these concepts allows you to:
- Ask the right questions during data discussions
- Validate requirements with engineering teams
- Interpret data analysis results
- Make informed product decisions based on user behavior patterns

## Interview Tips

- Focus on business logic, not syntax perfection
- Explain what insights each query would provide
- Think about how the data answers product questions
- Connect SQL results to user stories and business metrics
- Practice explaining queries in plain English

Remember: As a product owner, you're not expected to write perfect SQL, but understanding these concepts helps you communicate data needs effectively and make better product decisions.
# SQL Clauses - Detailed Reference Guide

## Introduction

This comprehensive guide explains various SQL clauses, their differences, use cases, and provides detailed examples. Understanding these clauses is essential for effective data querying and analysis.

## Sample Database Schema

We'll use an expanded e-commerce database for all examples:

**customers**
| customer_id | first_name | last_name | email | phone | city | country | registration_date |
|-------------|------------|-----------|-------|-------|------|---------|------------------|
| 1 | John | Smith | john.smith@email.com | +1-555-0101 | New York | USA | 2024-01-15 |
| 2 | Maria | Garcia | maria.garcia@email.com | +34-600-123456 | Madrid | Spain | 2024-02-20 |
| 3 | Chen | Wei | chen.wei@email.com | +86-138-0013-8000 | Beijing | China | 2024-01-10 |
| 4 | Sarah | Johnson | sarah.j@email.com | +1-555-0202 | Los Angeles | USA | 2024-03-05 |
| 5 | Ahmed | Al-Rashid | ahmed.rashid@email.com | +971-50-123-4567 | Dubai | UAE | 2024-02-28 |

**orders**
| order_id | customer_id | total_amount | order_date | status | shipping_country |
|----------|-------------|--------------|------------|--------|------------------|
| 101 | 1 | 150.00 | 2024-01-20 | completed | USA |
| 102 | 2 | 75.50 | 2024-02-25 | completed | Spain |
| 103 | 1 | 200.00 | 2024-03-01 | completed | USA |
| 104 | 3 | 50.00 | 2024-03-10 | pending | China |
| 105 | 4 | 300.00 | 2024-03-15 | completed | USA |
| 106 | 5 | 125.75 | 2024-03-20 | shipped | UAE |
| 107 | 2 | 89.99 | 2024-04-01 | cancelled | Spain |

---

## 1. WHERE Clause

**Purpose:** Filters individual rows based on specified conditions **before** any grouping occurs.

### Basic WHERE Examples

```sql
-- Simple equality
SELECT * FROM customers 
WHERE country = 'USA';

-- Numeric comparison
SELECT * FROM orders 
WHERE total_amount > 100;

-- Date comparison
SELECT * FROM customers 
WHERE registration_date >= '2024-02-01';
```

### WHERE with Multiple Conditions

```sql
-- AND operator (both conditions must be true)
SELECT first_name, last_name, city 
FROM customers 
WHERE country = 'USA' AND city = 'New York';

-- OR operator (either condition can be true)
SELECT * FROM orders 
WHERE status = 'completed' OR status = 'shipped';

-- Combining AND/OR with parentheses
SELECT * FROM orders 
WHERE (status = 'completed' OR status = 'shipped') 
  AND total_amount > 100;
```

### WHERE with IN and NOT IN

```sql
-- IN operator (matches any value in the list)
SELECT * FROM customers 
WHERE country IN ('USA', 'Spain', 'China');

-- NOT IN operator (excludes values in the list)
SELECT * FROM orders 
WHERE status NOT IN ('cancelled', 'pending');
```

### WHERE with NULL Values

```sql
-- Find records with NULL values
SELECT * FROM customers 
WHERE phone IS NULL;

-- Find records without NULL values
SELECT * FROM customers 
WHERE phone IS NOT NULL;

-- Note: Never use = NULL or != NULL, always use IS NULL or IS NOT NULL
```

---

## 2. LIKE Clause

**Purpose:** Pattern matching for text fields using wildcards.

### LIKE Wildcards
- `%` - Matches zero or more characters
- `_` - Matches exactly one character

### Basic LIKE Examples

```sql
-- Names starting with 'J'
SELECT first_name, last_name 
FROM customers 
WHERE first_name LIKE 'J%';

-- Names ending with 'a'
SELECT first_name, last_name 
FROM customers 
WHERE first_name LIKE '%a';

-- Names containing 'ar'
SELECT first_name, last_name 
FROM customers 
WHERE first_name LIKE '%ar%';

-- Names with exactly 4 characters
SELECT first_name, last_name 
FROM customers 
WHERE first_name LIKE '____';  -- Four underscores
```

### Advanced LIKE Patterns

```sql
-- Email addresses from specific domains
SELECT first_name, last_name, email 
FROM customers 
WHERE email LIKE '%@email.com';

-- Phone numbers with specific country codes
SELECT first_name, phone 
FROM customers 
WHERE phone LIKE '+1-%';

-- Names starting with 'S' and ending with 'h'
SELECT first_name, last_name 
FROM customers 
WHERE last_name LIKE 'S%h';

-- Second letter is 'a'
SELECT first_name 
FROM customers 
WHERE first_name LIKE '_a%';
```

### Case Sensitivity and ILIKE

```sql
-- Case-sensitive search (default in most databases)
SELECT first_name 
FROM customers 
WHERE first_name LIKE 'john%';  -- Won't match 'John'

-- Case-insensitive search (PostgreSQL)
SELECT first_name 
FROM customers 
WHERE first_name ILIKE 'john%';  -- Will match 'John'

-- Case-insensitive in MySQL/SQL Server
SELECT first_name 
FROM customers 
WHERE UPPER(first_name) LIKE UPPER('john%');
```

---

## 3. WHERE vs HAVING - Key Differences

This is one of the most important distinctions in SQL.

### WHERE Clause
- Filters **individual rows** before grouping
- Cannot use aggregate functions (COUNT, SUM, AVG, etc.)
- Processes data **before** GROUP BY

### HAVING Clause
- Filters **groups** after grouping
- Can use aggregate functions
- Processes data **after** GROUP BY

### Side-by-Side Comparison

```sql
-- WHERE Example: Filter individual orders before grouping
SELECT customer_id, COUNT(*) as order_count, SUM(total_amount) as total_spent
FROM orders 
WHERE total_amount > 100  -- Filters individual orders first
GROUP BY customer_id;

-- Result: Only considers orders > $100, then groups by customer

-- HAVING Example: Filter groups after aggregation
SELECT customer_id, COUNT(*) as order_count, SUM(total_amount) as total_spent
FROM orders 
GROUP BY customer_id
HAVING SUM(total_amount) > 200;  -- Filters groups after calculation

-- Result: Considers all orders, groups by customer, then shows only customers who spent > $200 total
```

### Practical Examples

```sql
-- WHERE: Show individual high-value orders from completed status
SELECT order_id, customer_id, total_amount
FROM orders 
WHERE status = 'completed' AND total_amount > 150;

-- HAVING: Show customers with multiple completed orders
SELECT customer_id, COUNT(*) as completed_orders
FROM orders 
WHERE status = 'completed'  -- First filter: only completed orders
GROUP BY customer_id
HAVING COUNT(*) > 1;        -- Then filter: only customers with >1 order

-- Combining WHERE and HAVING
SELECT 
    c.country,
    COUNT(*) as customer_count,
    AVG(o.total_amount) as avg_order_value
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE o.status = 'completed'    -- Filter: only completed orders
GROUP BY c.country
HAVING COUNT(*) >= 2           -- Filter: countries with 2+ customers
ORDER BY avg_order_value DESC;
```

---

## 4. ORDER BY Clause

**Purpose:** Sorts the result set by one or more columns.

### Basic ORDER BY

```sql
-- Ascending order (default)
SELECT first_name, last_name 
FROM customers 
ORDER BY last_name;

-- Descending order
SELECT first_name, last_name 
FROM customers 
ORDER BY last_name DESC;

-- Multiple columns
SELECT first_name, last_name, country 
FROM customers 
ORDER BY country, last_name;  -- First by country, then by last_name within each country
```

### ORDER BY with Different Data Types

```sql
-- Sorting numbers
SELECT order_id, total_amount 
FROM orders 
ORDER BY total_amount DESC;

-- Sorting dates
SELECT customer_id, registration_date 
FROM customers 
ORDER BY registration_date DESC;  -- Newest first

-- Sorting with NULL values
SELECT first_name, phone 
FROM customers 
ORDER BY phone NULLS LAST;  -- NULLs appear at the end
```

### Advanced ORDER BY

```sql
-- Conditional sorting with CASE
SELECT first_name, last_name, country
FROM customers 
ORDER BY 
    CASE 
        WHEN country = 'USA' THEN 1
        WHEN country = 'Spain' THEN 2
        ELSE 3
    END,
    last_name;

-- Sorting by column position (not recommended, but possible)
SELECT first_name, last_name, country 
FROM customers 
ORDER BY 3, 2;  -- Sort by 3rd column (country), then 2nd column (last_name)
```

---

## 5. GROUP BY Clause

**Purpose:** Groups rows with the same values into summary rows.

### Basic GROUP BY

```sql
-- Count customers by country
SELECT country, COUNT(*) as customer_count
FROM customers 
GROUP BY country;

-- Sum orders by customer
SELECT customer_id, SUM(total_amount) as total_spent
FROM orders 
GROUP BY customer_id;
```

### GROUP BY with Multiple Columns

```sql
-- Group by country and city
SELECT country, city, COUNT(*) as customer_count
FROM customers 
GROUP BY country, city
ORDER BY country, city;

-- Monthly revenue by country
SELECT 
    shipping_country,
    DATE_FORMAT(order_date, '%Y-%m') as month,
    SUM(total_amount) as monthly_revenue
FROM orders 
GROUP BY shipping_country, DATE_FORMAT(order_date, '%Y-%m')
ORDER BY shipping_country, month;
```

### Common GROUP BY Patterns

```sql
-- Customer segmentation by order frequency
SELECT 
    customer_id,
    COUNT(*) as order_count,
    CASE 
        WHEN COUNT(*) >= 3 THEN 'Frequent'
        WHEN COUNT(*) = 2 THEN 'Regular'
        ELSE 'Occasional'
    END as customer_type
FROM orders 
GROUP BY customer_id;

-- Revenue analysis by status
SELECT 
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_order_value,
    MIN(total_amount) as min_order,
    MAX(total_amount) as max_order
FROM orders 
GROUP BY status;
```

---

## 6. LIMIT and OFFSET

**Purpose:** Control how many rows are returned and where to start.

### Basic LIMIT

```sql
-- Get top 3 highest orders
SELECT order_id, total_amount 
FROM orders 
ORDER BY total_amount DESC 
LIMIT 3;

-- Get 5 most recent customers
SELECT first_name, last_name, registration_date 
FROM customers 
ORDER BY registration_date DESC 
LIMIT 5;
```

### LIMIT with OFFSET (Pagination)

```sql
-- Skip first 2 records, get next 3 (useful for pagination)
SELECT first_name, last_name 
FROM customers 
ORDER BY customer_id 
LIMIT 3 OFFSET 2;

-- Alternative syntax in some databases
SELECT first_name, last_name 
FROM customers 
ORDER BY customer_id 
LIMIT 2, 3;  -- MySQL syntax: LIMIT offset, count
```

### Practical Pagination Examples

```sql
-- Page 1 (first 10 customers)
SELECT customer_id, first_name, last_name 
FROM customers 
ORDER BY customer_id 
LIMIT 10 OFFSET 0;

-- Page 2 (next 10 customers)
SELECT customer_id, first_name, last_name 
FROM customers 
ORDER BY customer_id 
LIMIT 10 OFFSET 10;

-- Page 3 (next 10 customers)
SELECT customer_id, first_name, last_name 
FROM customers 
ORDER BY customer_id 
LIMIT 10 OFFSET 20;
```

---

## 7. DISTINCT Clause

**Purpose:** Remove duplicate rows from the result set.

### Basic DISTINCT

```sql
-- Get unique countries
SELECT DISTINCT country 
FROM customers;

-- Get unique order statuses
SELECT DISTINCT status 
FROM orders;
```

### DISTINCT with Multiple Columns

```sql
-- Get unique country-city combinations
SELECT DISTINCT country, city 
FROM customers;

-- Count unique customers who placed orders
SELECT COUNT(DISTINCT customer_id) as unique_customers
FROM orders;
```

### DISTINCT vs GROUP BY

```sql
-- These queries produce the same result:

-- Using DISTINCT
SELECT DISTINCT country 
FROM customers;

-- Using GROUP BY
SELECT country 
FROM customers 
GROUP BY country;

-- But GROUP BY allows aggregation:
SELECT country, COUNT(*) as customer_count
FROM customers 
GROUP BY country;  -- DISTINCT cannot do this
```

---

## 8. Combining Multiple Clauses

**Order of Execution:** Understanding the logical order helps write better queries.

### SQL Execution Order
1. **FROM** - Get the table(s)
2. **JOIN** - Combine tables
3. **WHERE** - Filter individual rows
4. **GROUP BY** - Group rows
5. **HAVING** - Filter groups
6. **SELECT** - Choose columns
7. **DISTINCT** - Remove duplicates
8. **ORDER BY** - Sort results
9. **LIMIT/OFFSET** - Limit results

### Complex Query Example

```sql
-- Find top 3 countries by customer count, but only for countries 
-- with customers who made completed orders above $100
SELECT 
    c.country,
    COUNT(DISTINCT c.customer_id) as customer_count,
    AVG(o.total_amount) as avg_order_value
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
WHERE o.status = 'completed'        -- Filter individual orders
  AND o.total_amount > 100          -- Filter individual orders
GROUP BY c.country                  -- Group by country
HAVING COUNT(DISTINCT c.customer_id) >= 1  -- Filter groups
ORDER BY customer_count DESC        -- Sort by customer count
LIMIT 3;                           -- Get top 3
```

---

## 9. Common Pitfalls and Best Practices

### WHERE vs HAVING Mistakes

```sql
-- ❌ WRONG: Cannot use aggregate functions in WHERE
SELECT customer_id, COUNT(*) as order_count
FROM orders 
WHERE COUNT(*) > 2  -- ERROR!
GROUP BY customer_id;

-- ✅ CORRECT: Use HAVING for aggregate conditions
SELECT customer_id, COUNT(*) as order_count
FROM orders 
GROUP BY customer_id
HAVING COUNT(*) > 2;

-- ❌ WRONG: Using HAVING instead of WHERE for non-aggregate conditions
SELECT customer_id, COUNT(*) as order_count
FROM orders 
GROUP BY customer_id
HAVING status = 'completed';  -- Inefficient!

-- ✅ CORRECT: Use WHERE for non-aggregate conditions
SELECT customer_id, COUNT(*) as order_count
FROM orders 
WHERE status = 'completed'
GROUP BY customer_id;
```

### LIKE Performance Tips

```sql
-- ❌ SLOW: Leading wildcard prevents index usage
SELECT * FROM customers 
WHERE first_name LIKE '%ohn';

-- ✅ FASTER: Trailing wildcard can use index
SELECT * FROM customers 
WHERE first_name LIKE 'John%';

-- ✅ BEST: Exact match when possible
SELECT * FROM customers 
WHERE first_name = 'John';
```

### GROUP BY Rules

```sql
-- ❌ WRONG: All non-aggregate columns must be in GROUP BY
SELECT customer_id, first_name, COUNT(*) 
FROM customers 
GROUP BY customer_id;  -- Missing first_name in GROUP BY

-- ✅ CORRECT: Include all non-aggregate columns in GROUP BY
SELECT customer_id, first_name, COUNT(*) 
FROM customers 
GROUP BY customer_id, first_name;
```

## Summary

Understanding these SQL clauses and their differences is crucial for effective data analysis:

- **WHERE**: Filters rows before grouping
- **LIKE**: Pattern matching with wildcards
- **HAVING**: Filters groups after aggregation
- **GROUP BY**: Creates groups for aggregation
- **ORDER BY**: Sorts results
- **LIMIT/OFFSET**: Controls result size and pagination
- **DISTINCT**: Removes duplicates

The key to mastering SQL is understanding when to use each clause and how they work together to answer business questions effectively.
