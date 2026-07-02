package com.huspy.interview.task2_inmemory_cache.solution.order_chache;

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
    // Structure must support multi-indexing.
    // We map userId -> List<Order> to ensure O(1) lookups by userId.
    private final Map<String, List<Order>> userOrdersIndex = new ConcurrentHashMap<>();

    // OPTIONAL: Primary key index.
    private final Set<String> orderIdIndex = ConcurrentHashMap.newKeySet();

    @Override
    public void save(Order order) {
        // Fail-fast validation. Never trust incoming data in a cache.
        if (order == null) throw new IllegalArgumentException("Order cannot be null");

        if (order.id == null || order.userId == null) throw new IllegalArgumentException("Order ID and User ID cannot be null");

        // OPTIONAL: Idempotency check. If the exact same order object is already saved, skip.
        if (!orderIdIndex.add(order.id)) return;

        // Atomic operation on ConcurrentHashMap, computeIfAbsent prevents race conditions where two threads try to initialize the list for the same userId.
        // - CopyOnWriteArrayList is thread-safe for reads/writes, but fits best if reads heavily outnumber writes.
        // - Alternative: Collections.synchronizedList(new ArrayList<>()) if writes are frequent.
        try {
            userOrdersIndex
                    .computeIfAbsent(order.userId, k -> new CopyOnWriteArrayList<>())
                    .add(order);
        } catch (Throwable t) {
            orderIdIndex.remove(order.id);
            throw t;
        }
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
}

interface OrderCache {
    void save(Order order);

    List<Order> getByUserId(String userId);
}

class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}