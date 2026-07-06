package com.huspy.interview.task1_refactoring.solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrderManager {

    // FIX minor: Use a proper logger instead of System.out.
    private static final Logger log = LoggerFactory.getLogger(OrderManager.class);

    // FIX moderate: Extract magic number to a constant.
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("10.50");

    // FIX major: Spring beans are singletons. Use thread-safe collections or make the service stateless.
    private final Set<String> processedOrders = ConcurrentHashMap.newKeySet();

    public void processData(Order req) {
        // FIX major: Fail-fast guard clause.
        // We throw an exception instead of a silent 'return' to prevent masking bugs.
        // A null order is a critical contract violation
        if (req == null) {
            log.warn("Received null order request.");
            throw new IllegalArgumentException("Order cannot be null");
        }

        // FIX moderate: Idempotency check. No need to check req.id for null anymore!
        if (processedOrders.contains(req.getId())) {
            log.info("Order {} is already processed. Skipping.", req.getId());
            return;
        }

        if (req.getStatus().equals("NEW")) {
            try {
                Order discountedOrder = req.withDiscount(DISCOUNT_AMOUNT);
                updateOrderStatusInDb(discountedOrder);
                // FIX moderate: Add to the processed list ONLY after successful execution
                // to avoid inconsistent state if an exception is thrown above.
                processedOrders.add(discountedOrder.getId());

            } catch (OrderUpdateException e) {
                // FIX major: Log the exception properly and rethrow it.
                log.error("Error processing order id: {}", req.getId(), e);
                throw new RuntimeException("Order processing failed", e);
            }
        }
    }

    private void updateOrderStatusInDb(Order req) throws OrderUpdateException {
        log.info("Saving to DB...");
    }
}

class Order {
    // FIX: major: made the fields private
    final private String id;
    final private String status;
    // FIX major: Changed double to BigDecimal for money.
    final private BigDecimal price;

    // FIX moderate: Encapsulated validation inside the constructor (Always-Valid Domain Model).
    // It is impossible to instantiate an Order in an invalid or partial state.
    public Order(String id, String status, BigDecimal price) {
        if (id == null || status == null || price == null) {
            throw new IllegalArgumentException("Order fields cannot be null");
        }
        this.id = id;
        this.status = status;
        this.price = price;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public BigDecimal getPrice() { return price; }

    // FIX major: Instead of mutating 'this', we return a brand NEW instance with updated price.
    public Order withDiscount(BigDecimal discount) {
        return new Order(this.id, this.status, this.price.subtract(discount));
    }
}

class OrderUpdateException extends Exception {
    public OrderUpdateException(String message) {
        super(message);
    }
}