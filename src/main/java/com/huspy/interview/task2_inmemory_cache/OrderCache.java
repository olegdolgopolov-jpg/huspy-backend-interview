package com.huspy.interview.task2_inmemory_cache;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Thread-safe in-memory cache for storing and retrieving orders.
 * Implementations must support concurrent access without data corruption.
 */
public interface OrderCache {

    /**
     * Saves an order to the cache.
     * Implementation must be thread-safe and handle duplicate orders appropriately.
     *
     * @param order the order to save (must not be null)
     * @throws IllegalArgumentException if order is null or has null required fields
     */
    void save(Order order);

    /**
     * Retrieves all orders for a given user ID.
     * Implementation should provide efficient lookups (O(1) or near O(1)).
     *
     * @param userId the user ID to search for (must not be null)
     * @return list of orders for the user, or empty list if none found (never null)
     * @throws IllegalArgumentException if userId is null
     */
    List<Order> getByUserId(String userId);

    // OPTIONAL METHODS (if time permits):

    /**
     * Returns the total sales amount for a specific date.
     * Implementation should provide efficient lookups (O(1) preferred).
     *
     * @param date the date to query (must not be null)
     * @return total sales amount for the date, or BigDecimal.ZERO if no sales
     * @throws IllegalArgumentException if date is null
     */
    // BigDecimal getTotalSalesByDate(LocalDate date);

    /**
     * Returns the total sales amount for a date range (inclusive).
     * Implementation should provide efficient range queries.
     *
     * @param dateFrom the start date (inclusive, must not be null)
     * @param dateTo the end date (inclusive, must not be null)
     * @return total sales amount for the date range, or BigDecimal.ZERO if no sales
     * @throws IllegalArgumentException if dates are null or dateFrom is after dateTo
     */
    // BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo);
}

/**
 * Represents an order in the system.
 */
class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}