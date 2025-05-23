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
