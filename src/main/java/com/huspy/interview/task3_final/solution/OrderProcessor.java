package com.huspy.interview.task3_final.solution;

import java.util.List;

/**
 * Thread-safe in-memory processor and cache for orders.
 * Must handle concurrent incoming requests efficiently.
 */
public interface OrderProcessor {

    /**
     * Processes and saves an incoming order.
     * <p>
     * Requirements:
     * 1. Deduplication: Orders with an already seen ID must be ignored (idempotency).
     * 2. Promo logic: If the order status is "PROMO", apply a flat discount of 0.20 to the price.
     * 3. Validation: Price cannot be negative after discount.
     * 4. Storage: Save the successfully processed order to memory.
     *
     * @param order the order to process
     * @throws IllegalArgumentException if order is null or invalid
     */
    void processAndSave(Order order);

    /**
     * Retrieves all successfully processed orders for a given user.
     * Requirement: Must be highly efficient (O(1) lookup expected).
     *
     * @return list of orders, or empty list if none found. Never null.
     */
    List<Order> getOrdersByUser(String userId);

    /**
     * Retrieves all orders processed within a specific time window.
     * Requirement: Must not iterate over all orders in the system (avoid O(N) full scan).
     *
     * @param startTimestamp inclusive start time
     * @param endTimestamp inclusive end time
     * @return list of orders in the time window, or empty list.
     */
    List<Order> getOrdersInTimeWindow(long startTimestamp, long endTimestamp);
}
