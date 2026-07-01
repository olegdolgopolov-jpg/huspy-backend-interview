# Huspy Backend Interview Tasks

This repository contains two Java coding interview tasks designed to evaluate candidates' understanding of code quality, concurrency, data structures, and optimization techniques.

## Project Structure

```
src/main/java/com/huspy/interview/
├── task1_refactoring/
│   ├── OrderManager.java          # Initial code with issues
│   └── solution/
│       └── OrderManager.java       # Refactored solution
└── task2_inmemory_cache/
    ├── OrderCache.java             # Interface with basic requirements
    └── solution/
        ├── order_chache/           # Basic implementation
        ├── order_chache_with_total/# With date aggregation (optional)
        └── order_cache_with_total_for_user/ # With date range queries (optional)
```

---

## Task 1: Code Refactoring

**Location:** `src/main/java/com/huspy/interview/task1_refactoring/`

### Initial Code (`OrderManager.java`)

The initial implementation contains several critical issues:

```java
@Service
public class OrderManager {
    private List<String> processedOrders = new ArrayList<>();

    public void processData(Order req) {
        try {
            if (req.status.equals("NEW")) {
                double p = req.price - 10.50;
                req.price = p;
                processedOrders.add(req.id);
                updateOrderStatusInDb(req);
            }
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    @Transactional
    public void updateOrderStatusInDb(Order req) {
        System.out.println("Saving to DB...");
    }
}
```

### Issues to Identify

Candidates should identify and fix the following issues:

#### Major Issues:
1. **Thread Safety**: `ArrayList` is not thread-safe in a Spring singleton service
2. **Monetary Precision**: Using `double` for money causes precision loss
3. **Transaction Management**: `@Transactional` on a private method is ineffective (Spring AOP limitation)
4. **Null Safety**: No null checks for incoming `Order` objects
5. **Error Handling**: Silent exception swallowing without proper logging or rethrowing
6. **State Consistency**: Order added to `processedOrders` before DB update completes

#### Moderate Issues:
1. **Magic Numbers**: Hardcoded discount value `10.50`
2. **Idempotency**: No check to prevent duplicate processing
3. **Logging**: Using `System.out.println` instead of proper logger

### Solution Highlights (`solution/OrderManager.java`)

The refactored solution addresses all issues:

- Uses `ConcurrentHashMap.newKeySet()` for thread-safe order tracking
- Uses `BigDecimal` for monetary calculations
- Extracts discount to a constant: `DISCOUNT_AMOUNT`
- Implements proper logging with SLF4J
- Adds fail-fast validation with `IllegalArgumentException`
- Implements idempotency check to prevent duplicate processing
- Properly rethrows exceptions to trigger transaction rollback
- Adds orders to processed set only after successful execution
- Implements constructor validation in the `Order` class (Always-Valid Domain Model)

**Key Learning Points:** Thread safety in Spring services, proper transaction boundaries, BigDecimal for money, defensive programming

---

## Task 2: In-Memory Cache Implementation

**Location:** `src/main/java/com/huspy/interview/task2_inmemory_cache/`

### Requirements

Implement a thread-safe in-memory cache for orders with the following interface:

```java
public interface OrderCache {
    void save(Order order);
    List<Order> getByUserId(String userId);
    // Optional: BigDecimal getTotalSalesByDate(LocalDate date);
    // Optional: BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo);
}

class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}
```

### Core Requirements:
- Thread-safe implementation
- Efficient lookups by `userId` (O(1) or near O(1))
- Proper handling of null values
- No data corruption under concurrent access

### Optional Requirements (if time permits):
1. **`getTotalSalesByDate(LocalDate date)`** - Return total sales for a specific date
2. **`getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo)`** - Return total sales for a date range

---

## Solution Approaches

### 1. Basic Implementation (`solution/order_chache/`)

**File:** `OrderCacheImpl.java`

**Key Features:**
- `ConcurrentHashMap<String, List<Order>>` for userId index (O(1) lookup)
- `CopyOnWriteArrayList` for thread-safe order storage per user
- `ConcurrentHashMap.newKeySet()` for idempotency tracking
- Defensive copying with `Collections.unmodifiableList()`
- Fail-fast validation for null inputs
- Returns empty list instead of null (clean code practice)

**Time Complexity:**
- `save()`: O(1)
- `getByUserId()`: O(1)

---

### 2. With Date Aggregation (`solution/order_chache_with_total/`)

**File:** `OrderCacheIWithTotal.java`

**Additional Features:**
- Implements `getTotalSalesByDate(LocalDate date)`
- Adds `ConcurrentHashMap<LocalDate, BigDecimal>` for date-based sales aggregation
- Uses `merge()` for atomic, thread-safe total updates
- Pre-computes totals on write for O(1) reads

**Time Complexity:**
- `save()`: O(1)
- `getByUserId()`: O(1)
- `getTotalSalesByDate()`: O(1)

**Trade-offs:** Slightly more memory and write overhead for fast aggregated reads

---

### 3. With Date Range Queries (`solution/order_cache_with_total_for_user/`)

**File:** `OrderCacheIWithTotal.java`

**Additional Features:**
- Implements `getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo)`
- Uses `ConcurrentSkipListMap<LocalDate, BigDecimal>` (NavigableMap)
- Enables efficient range queries with `subMap()`
- Maintains sorted keys for O(log N) range extraction

**Time Complexity:**
- `save()`: O(log N) due to tree insertion
- `getByUserId()`: O(1)
- `getTotalSalesByDateRange()`: O(log N + K) where K is number of dates in range

**Alternative Approach (commented):**
- Manual iteration using `ceilingKey()` and `higherKey()`
- O(K * log N) complexity - less efficient but more explicit

**Trade-offs:** Slower writes (O(log N) vs O(1)) for efficient range queries

---

## Evaluation Criteria

### Task 1 - Refactoring:
- Ability to identify concurrency issues
- Understanding of Spring framework limitations (AOP, transactions)
- Knowledge of proper data types for monetary values
- Error handling and logging best practices
- Code organization and maintainability

### Task 2 - In-Memory Cache:
- Understanding of concurrent data structures
- Efficient indexing strategies
- Defensive programming (validation, immutability)
- Performance considerations (time/space complexity)
- Optional: Advanced data structure usage (NavigableMap, TreeMap)

---

## Running the Code

This is a Spring Boot project. Use Gradle or your IDE to compile and run tests.

```bash
./gradlew build
./gradlew test
```

---

## Interview Tips

### For Interviewers:
- **Task 1**: Start with asking candidate to review the code and identify issues. Expected time: 15-20 minutes
- **Task 2**: Begin with basic implementation. If completed quickly, introduce optional methods. Expected time: 20-30 minutes
- **Discussion Points**: Trade-offs, scalability, alternative approaches, production considerations

### For Candidates:
- Think out loud about your approach
- Ask clarifying questions about requirements
- Consider concurrency implications
- Discuss trade-offs of your design choices
- Don't forget edge cases (null values, empty results, invalid input)
