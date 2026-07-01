package com.huspy.interview.task2_inmemory_cache.solution.order_cache_with_total_for_user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

class OrderCacheImpl implements OrderCache {

    // Must use a thread-safe map. HashMap will crash or corrupt data under heavy load.
    // We map userId -> List<Order> to ensure O(1) or near O(1) lookups by userId.
    private final Map<String, List<Order>> userOrdersIndex = new ConcurrentHashMap<>();

    // OPTIONAL: Primary key index.
    private final Set<String> orderIdIndex = ConcurrentHashMap.newKeySet();

    // A tree/skip-list structure keeps keys sorted, enabling efficient O(log N) range queries.
    private final NavigableMap<LocalDate, BigDecimal> salesByDateIndex = new ConcurrentSkipListMap<>();

    @Override
    public void save(Order order) {
        // Fail-fast validation. Never trust incoming data in a cache.
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.id == null || order.userId == null) {
            throw new IllegalArgumentException("Order ID and User ID cannot be null");
        }

        // Idempotency check. If the exact same order object is already saved, skip.
        if (!orderIdIndex.add(order.id)) return;

        // Atomic operation on ConcurrentHashMap.
        // computeIfAbsent prevents race conditions where two threads try to initialize the list for the same userId.
        // CopyOnWriteArrayList is thread-safe for reads/writes, but fits best if reads heavily outnumber writes.
        // Alternative: Collections.synchronizedList(new ArrayList<>()) if writes are frequent.
        userOrdersIndex
                .computeIfAbsent(order.userId, k -> new CopyOnWriteArrayList<>())
                .add(order);

        // Pre-compute (aggregate) sales on write.
        // We use merge() for atomic, thread-safe updates of the aggregated total.
        salesByDateIndex.merge(order.orderDate, order.amount, BigDecimal::add);
    }

    @Override
    public List<Order> getByUserId(String userId) {
        // Fail-fast validation.
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<Order> orders = userOrdersIndex.get(userId);

        // Defensive copying / Unmodifiable view.
        // Never return the raw internal mutable list to the caller, as they could modify it and corrupt the cache.
        // Return an empty list instead of null if no orders are found (Clean Code best practice).
        return orders != null ? Collections.unmodifiableList(orders) : Collections.emptyList();
    }

    @Override
    public BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        // Validation to prevent invalid ranges.
        if (dateFrom == null || dateTo == null) throw new IllegalArgumentException("Date bounds cannot be null");

        if (dateFrom.isAfter(dateTo)) throw new IllegalArgumentException("dateFrom cannot be after dateTo");

        // Efficient range extraction using subMap().
        // subMap() returns a view of the sorted map within the range in O(log N) time.
        // True as parameters ensure the bounds are inclusive: [dateFrom, dateTo].
        NavigableMap<LocalDate, BigDecimal> subMap = salesByDateIndex.subMap(dateFrom, true, dateTo, true);

        // Aggregate the pre-computed totals for the matching dates.
        return subMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * A bit worse solution from both readability and performance perspective, but still working.
     * O(K * logN) instead of O(logN) for the best case.
    @Override
    public BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        // Validation to prevent invalid ranges
        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Date bounds cannot be null");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }

        BigDecimal total = BigDecimal.ZERO;

        // Find the first key that is >= dateFrom (O(log N) complexity)
        LocalDate currentKey = salesByDateIndex.ceilingKey(dateFrom);

        // Iterate through the sorted map until we run out of keys or exceed dateTo
        while (currentKey != null && !currentKey.isAfter(dateTo)) {

            // Fetch the value for the current key (O(log N) lookup)
            BigDecimal amount = salesByDateIndex.get(currentKey);
            if (amount != null) {
                total = total.add(amount);
            }

            // Move to the strictly next key in the tree (O(log N) complexity)
            currentKey = salesByDateIndex.higherKey(currentKey);
        }

        return total;
    }
    */

}

interface OrderCache {
    void save(Order order);

    List<Order> getByUserId(String userId);

    BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo);
}

class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}