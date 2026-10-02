package com.huspy.interview.task3_final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe in-memory processor and cache for orders.
 * Must handle concurrent incoming requests efficiently.
 */
public interface OrderProcessor {

    /**
     * Processes and saves an incoming order.
     *
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
}

class Order {
    public String id;
    public String userId;
    public String status;

    // CRITICAL (Data Type): Using 'double' for money leads to floating-point precision issues
    // (e.g., 0.30 - 0.20 = 0.09999999999999998). Must be BigDecimal.
    // MINOR (Encapsulation): Fields are public and mutable. In a multi-threaded cache,
    // it's safer to use immutable objects (final fields, no setters).
    public double price;
    public long timestamp;

    public Order(String id, String userId, String status, double price, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.price = price;
        this.timestamp = timestamp;
    }
}

class OrderProcessorImpl implements OrderProcessor {

    // MAJOR (Thread-Safety): ArrayList is not thread-safe.
    // MINOR (Performance): List.contains() is O(N). For deduplication, this must be a Set.
    // Fix: Set<String> processedOrderIds = ConcurrentHashMap.newKeySet();
    private List<String> processedOrderIds = new ArrayList<>();

    // MAJOR (Thread-Safety): HashMap is not thread-safe and can cause data loss or infinite loops.
    // Fix: Map<String, List<Order>> userOrders = new ConcurrentHashMap<>();
    private Map<String, List<Order>> userOrders = new HashMap<>();

    @Override
    public void processAndSave(Order order) {
        // MINOR (Validation): Missing fail-fast check for null order.
        // If order is null, order.id will throw an ungraceful NullPointerException.

        try {
            // 1. Deduplication check
            if (processedOrderIds.contains(order.id)) {
                return;
            }

            // CRITICAL (State Corruption): ID is added to the deduplication list BEFORE validation.
            // If the order fails validation later (e.g., negative price), it will throw an exception,
            // but the ID remains in 'processedOrderIds'. The order gets "stuck" and can never be retried.
            // Fix: Move this down or remove ID in the catch block (rollback).
            processedOrderIds.add(order.id);

            // 2. Promo logic
            if ("PROMO".equals(order.status)) {
                order.price = order.price - 0.20;
            }

            // 3. Validation
            if (order.price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }

            // 4. Save to memory
            // MAJOR (Race Condition): Check-then-act on a Map is not atomic.
            // Two threads might concurrently see that orders == null and overwrite each other's lists.
            // Fix: userOrders.computeIfAbsent(order.userId, k -> new CopyOnWriteArrayList<>()).add(order);
            List<Order> orders = userOrders.get(order.userId);
            if (orders == null) {
                // MAJOR (Thread-Safety): ArrayList inside the map is not thread-safe for concurrent reads/writes.
                // Fix: use CopyOnWriteArrayList or Collections.synchronizedList.
                orders = new ArrayList<>();
                userOrders.put(order.userId, orders);
            }
            orders.add(order);

        } catch (Exception e) {
            // CRITICAL (Swallowing Exceptions): The interface contract explicitly says
            // @throws IllegalArgumentException, but this try-catch blocks it and just prints to console.
            // The caller will never know the order failed.
            // Fix: Remove generic try-catch, or catch/rollback/rethrow.
            System.out.println("Error processing order: " + order.id);
        }
    }

    @Override
    public List<Order> getOrdersByUser(String userId) {
        // MAJOR (Contract Violation): Interface says "return empty list if none found. Never null."
        // Map.get() returns null if the userId is not present.
        // Fix: return userOrders.getOrDefault(userId, Collections.emptyList());

        // MINOR (Encapsulation): Returning the internal mutable list directly.
        // The caller can modify the returned list and corrupt the cache.
        // Fix: Return an unmodifiable view of the list.
        return userOrders.get(userId);
    }
}
