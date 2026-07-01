package com.huspy.interview.task2_inmemory_cache.solution.order_chache_with_total;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class OrderCacheImpl implements OrderCache {

    // Must use a thread-safe map. HashMap will crash or corrupt data under heavy load.
    // FIX major: Structure must support multi-indexing.
    // We map userId -> List<Order> to ensure O(1) or near O(1) lookups by userId.
    private final Map<String, List<Order>> userOrdersIndex = new ConcurrentHashMap<>();

    // OPTIONAL: Primary key index.
    private final Set<String> orderIdIndex = ConcurrentHashMap.newKeySet();

    // Added a third index for date aggregation to achieve O(1) reads.
    // Maps LocalDate -> Total Sales Amount.
    private final Map<LocalDate, BigDecimal> salesByDateIndex = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        // FIX major: Fail-fast validation. Never trust incoming data in a cache.
        if (order == null) throw new IllegalArgumentException("Order cannot be null");

        if (order.id == null || order.userId == null) throw new IllegalArgumentException("Order ID and User ID cannot be null");

        // FIX moderate: Idempotency check. If the exact same order object is already saved, skip.
        if (!orderIdIndex.add(order.id)) return;

        // FIX major: Atomic operation on ConcurrentHashMap.
        // computeIfAbsent prevents race conditions where two threads try to initialize the list for the same userId.
        // FIX moderate: CopyOnWriteArrayList is thread-safe for reads/writes, but fits best if reads heavily outnumber writes.
        // Alternative: Collections.synchronizedList(new ArrayList<>()) if writes are frequent.
        userOrdersIndex
                .computeIfAbsent(order.userId, k -> new CopyOnWriteArrayList<>())
                .add(order);

        // NEW FIX major: Pre-compute (aggregate) sales on write.
        // We use merge() for atomic, thread-safe updates of the aggregated total.
        salesByDateIndex.merge(order.orderDate, order.amount, BigDecimal::add);
    }

    @Override
    public List<Order> getByUserId(String userId) {
        // Fail-fast validation.
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");

        List<Order> orders = userOrdersIndex.get(userId);

        // Defensive copying / Unmodifiable view.
        // Never return the raw internal mutable list to the caller, as they could modify it and corrupt the cache.
        // Return an empty list instead of null if no orders are found (Clean Code best practice).
        return orders != null ? Collections.unmodifiableList(orders) : Collections.emptyList();
    }

    @Override
    public BigDecimal getTotalSalesByDate(LocalDate date) {
        // Fail-fast validation.
        if (date == null) throw new IllegalArgumentException("Date cannot be null");

        // O(1) operation. We just fetch the pre-calculated value.
        // Return BigDecimal.ZERO instead of null if there were no sales on that day.
        return salesByDateIndex.getOrDefault(date, BigDecimal.ZERO);
    }
}

interface OrderCache {
    void save(Order order);

    List<Order> getByUserId(String userId);

    BigDecimal getTotalSalesByDate(LocalDate date);
}

class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}