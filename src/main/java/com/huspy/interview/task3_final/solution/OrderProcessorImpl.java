package com.huspy.interview.task3_final.solution;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrderProcessorImpl implements OrderProcessor {

    private static final BigDecimal PROMO_DISCOUNT = new BigDecimal("0.20");

    // FIX (MAJOR/MINOR): Using a thread-safe Set for atomic O(1) deduplication.
    private final Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();

    // FIX (MAJOR): Using a thread-safe map.
    private final Map<String, List<Order>> userOrders = new ConcurrentHashMap<>();

    // FIX (BONUS): Secondary index for time-based lookups.
    // ConcurrentSkipListMap keeps keys sorted and provides O(log N) time window queries.
    // We map timestamp -> List of Orders (because multiple orders can have the exact same timestamp).
    private final ConcurrentNavigableMap<Long, List<Order>> timeIndex = new ConcurrentSkipListMap<>();

    @Override
    public void processAndSave(Order order) {
        // FIX (MINOR): Fail-fast validation for null order to prevent unexpected NPEs.
        if (order == null || order.id() == null || order.userId() == null) {
            throw new IllegalArgumentException("Order and its identifiers cannot be null");
        }

        // FIX (MAJOR): Atomic check-and-add. If the ID already exists, add() returns false.
        // This eliminates the check-then-act race condition of using contains() followed by add().
        if (!processedOrderIds.add(order.id())) {
            return;
        }

        try {
            Order orderToSave = order;

            // 2. Promo logic
            if ("PROMO".equals(orderToSave.status())) {
                orderToSave = orderToSave.withDiscount(PROMO_DISCOUNT);
            }

            // 3. Validation
            // FIX (CRITICAL): BigDecimal comparison must be done using compareTo(), not operators.
            if (orderToSave.price().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }

            // 4. Save to memory
            // FIX (MAJOR): Atomic list creation if it doesn't exist using computeIfAbsent.
            // Using thread-safe CopyOnWriteArrayList for concurrent reads/writes.
            userOrders.computeIfAbsent(orderToSave.userId(), k -> new CopyOnWriteArrayList<>())
                    .add(orderToSave);

            // 5. Save to Time Index (Secondary Index by Timestamp)
            // Using computeIfAbsent to handle multiple orders arriving at the exact same millisecond
            timeIndex.computeIfAbsent(orderToSave.timestamp(), k -> new CopyOnWriteArrayList<>())
                    .add(orderToSave);

        } catch (Exception e) {
            // FIX (CRITICAL): State Rollback. If the business logic fails after adding the ID to the Set,
            // we must remove the ID from the processed set to allow the client to retry the order later.
            processedOrderIds.remove(order.id());

            // Rethrow the exception instead of swallowing it (System.out.println removed).
            throw e;
        }
    }

    @Override
    public List<Order> getOrdersByUser(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Order> orders = userOrders.get(userId);

        // FIX (MAJOR/MINOR): Return Collections.emptyList() if null to satisfy the contract.
        // Wrap in unmodifiableList so the caller cannot mutate and corrupt the internal cache.
        return orders != null ? Collections.unmodifiableList(orders) : Collections.emptyList();
    }

    @Override
    public List<Order> getOrdersInTimeWindow(long startTimestamp, long endTimestamp) {
        if (startTimestamp > endTimestamp) {
            throw new IllegalArgumentException("Start time must be before or equal to end time");
        }

        // Get a view of the map within the specified range (inclusive on both ends)
        var subMap = timeIndex.subMap(startTimestamp, true, endTimestamp, true);

        // Flatten the lists from the subMap into a single result list
        // Using Java Streams for clean code, .toList() returns an unmodifiable list (Java 16+)
        return subMap.values().stream()
                .flatMap(List::stream)
                .toList();
    }
}