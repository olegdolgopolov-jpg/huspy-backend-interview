# Huspy Backend Interview - Evaluation Checklist

Copy and paste this into Notion for easy candidate evaluation.

---

## TASK 1: Code Refactoring (`OrderManager.java`)

### Major Issues (Critical - Must Fix)

- [ ] **Thread Safety**: Replaced `ArrayList` with thread-safe collection (e.g., `ConcurrentHashMap.newKeySet()` or `Collections.synchronizedList()`)
- [ ] **Monetary Precision**: Changed `double` to `BigDecimal` for price calculations
- [ ] **Transaction Management**: Removed `@Transactional` from private method or made method public/moved to separate service
- [ ] **Null Safety**: Added null check for incoming `Order` object
- [ ] **Error Handling**: Replaced silent exception swallowing with proper logging and rethrowing
- [ ] **State Consistency**: Moved `processedOrders.add()` to execute AFTER successful DB update

### Moderate Issues (Important - Should Fix)

- [ ] **Magic Numbers**: Extracted hardcoded discount value `10.50` to a named constant
- [ ] **Idempotency**: Added check to prevent duplicate processing of same order ID
- [ ] **Logging**: Replaced `System.out.println` with proper logger (SLF4J/Log4j)
- [ ] **Order Validation**: Added validation in Order class (constructor or setters)

### Code Quality

- [ ] Code is readable and well-structured
- [ ] Proper naming conventions used
- [ ] Comments added where necessary (inline comments for complex logic)

### Bonus Points

- [ ] Discussed transaction boundary implications
- [ ] Mentioned Spring AOP limitations with private methods
- [ ] Suggested additional improvements (e.g., extracting discount logic, creating separate service)
- [ ] Implemented Always-Valid Domain Model pattern in Order class

---

## TASK 2: In-Memory Cache Implementation

### Core Requirements (Required)

#### Data Structures & Thread Safety
- [ ] Used thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`, etc.)
- [ ] Implemented proper indexing structure for O(1) userId lookups
- [ ] No race conditions or data corruption under concurrent access

#### `save(Order order)` Method
- [ ] Null validation for order object
- [ ] Null validation for required fields (id, userId)
- [ ] Thread-safe insertion logic
- [ ] Idempotency handling (optional but good to have)

#### `getByUserId(String userId)` Method
- [ ] Null validation for userId parameter
- [ ] Returns unmodifiable list or defensive copy (not raw internal list)
- [ ] Returns empty list instead of null when no orders found
- [ ] O(1) or near O(1) time complexity

#### Code Quality
- [ ] Proper validation with `IllegalArgumentException`
- [ ] Clean, readable code
- [ ] Efficient data structure choices explained

---

## TASK 2: Optional Methods (If Time Permits)

### Option 1: `getTotalSalesByDate(LocalDate date)`

#### Implementation
- [ ] Added date-based indexing structure (e.g., `Map<LocalDate, BigDecimal>`)
- [ ] Pre-computes totals during `save()` operation
- [ ] Uses atomic operations (`merge()` or similar) for thread-safe updates

#### Method Requirements
- [ ] Null validation for date parameter
- [ ] Returns `BigDecimal.ZERO` instead of null when no sales found
- [ ] O(1) time complexity for reads

---

### Option 2: `getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo)`

#### Implementation
- [ ] Used sorted/navigable data structure (`ConcurrentSkipListMap`, `TreeMap`)
- [ ] Efficient range query implementation (e.g., `subMap()`)
- [ ] Pre-computes totals during `save()` operation

#### Method Requirements
- [ ] Null validation for both date parameters
- [ ] Validates dateFrom is not after dateTo
- [ ] Returns `BigDecimal.ZERO` for empty ranges
- [ ] Efficient time complexity: O(log N + K) or better

#### Advanced Implementation
- [ ] Discussed trade-offs between write and read performance
- [ ] Explained time complexity implications
- [ ] Considered memory usage vs. query speed

---

## Overall Evaluation

### Communication & Problem Solving

- [ ] Asked clarifying questions about requirements
- [ ] Thought out loud and explained their approach
- [ ] Identified edge cases proactively
- [ ] Discussed trade-offs of design decisions

### Technical Knowledge

- [ ] Demonstrated understanding of concurrency issues
- [ ] Showed knowledge of appropriate data structures
- [ ] Understood time/space complexity implications
- [ ] Familiar with Java concurrent collections

### Code Quality

- [ ] Clean, maintainable code
- [ ] Proper error handling
- [ ] Defensive programming practices
- [ ] Good naming conventions

### Time Management

- [ ] **Task 1 Completed**: ⏱️ _____ minutes (Expected: 15-20)
- [ ] **Task 2 Core Completed**: ⏱️ _____ minutes (Expected: 15-20)
- [ ] **Task 2 Optional**: ⏱️ _____ minutes (Expected: 10-15 if attempted)
- [ ] **Total Time**: ⏱️ _____ minutes (Expected: 30-45 total)

---

## Scoring Guide

### Task 1: Code Refactoring (50 points)
- **Major Issues Fixed (30 points)**: 5 points each × 6 issues
- **Moderate Issues Fixed (15 points)**: 3-4 points each × 4 issues
- **Code Quality (5 points)**

### Task 2: Core Implementation (40 points)
- **Thread Safety & Data Structures (15 points)**
- **save() Implementation (10 points)**
- **getByUserId() Implementation (10 points)**
- **Code Quality & Validation (5 points)**

### Task 2: Optional Methods (10 bonus points)
- **getTotalSalesByDate (5 points)**
- **getTotalSalesByDateRange (5 points)**

### Overall Assessment (Max: 90-100 points with bonus)

**Total Score**: _____ / 90 (_____ / 100 with bonus)

---

## Final Rating

- [ ] **Strong Hire** (80+ points): Excellent technical skills, clean code, good communication
- [ ] **Hire** (65-79 points): Solid technical skills, minor improvements needed
- [ ] **Maybe** (50-64 points): Some technical gaps, needs mentoring
- [ ] **No Hire** (<50 points): Significant technical gaps or poor code quality

---

## Additional Notes

**Strengths:**
-
-
-

**Areas for Improvement:**
-
-
-

**Overall Impression:**


**Recommendation:** ☐ Strong Hire  ☐ Hire  ☐ Maybe  ☐ No Hire
