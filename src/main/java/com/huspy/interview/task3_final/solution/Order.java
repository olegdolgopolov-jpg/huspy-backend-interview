package com.huspy.interview.task3_final.solution;

import java.math.BigDecimal;

/**
 * @param id    FIX (MINOR): Made fields final to ensure immutability, which is safer for multi-threaded caching.
 * @param price FIX (CRITICAL): Changed double to BigDecimal to avoid floating-point precision issues with money.
 */
public record Order(String id, String userId, String status, BigDecimal price, long timestamp) {
    public Order {
        if (id == null || userId == null || status == null || price == null) {
            throw new IllegalArgumentException("Order fields cannot be null");
        }
    }

    // FIX (MINOR): Instead of mutating the existing object, return a new instance with the applied discount.
    public Order withDiscount(BigDecimal discount) {
        return new Order(this.id, this.userId, this.status, this.price.subtract(discount), this.timestamp);
    }
}