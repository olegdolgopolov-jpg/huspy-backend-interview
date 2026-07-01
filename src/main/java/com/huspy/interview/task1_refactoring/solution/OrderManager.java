package com.huspy.interview.task1_refactoring.solution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderManager {

    // FIX minor: Use a proper logger instead of System.out.
    private static final Logger log = LoggerFactory.getLogger(OrderManager.class);

    // FIX moderate: Extract magic number to a constant.
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("10.50");

    // FIX major: Spring beans are singletons. Use thread-safe collections or make the service stateless.
    private final Set<String> processedOrders = ConcurrentHashMap.newKeySet();

    // FIX major: Move @Transactional to the public method. Spring AOP ignores private methods and self-invocations.
    public void processData(Order req) {
        try {
            // FIX major: Fail-fast guard clause.
            // We throw an exception instead of a silent 'return' to prevent masking bugs.
            // A null order is a critical contract violation
            if (req == null) {
                log.warn("Received null order request.");
                throw new IllegalArgumentException("Order cannot be null");
            }

            // FIX moderate: Idempotency check. No need to check req.id for null anymore!
            if (processedOrders.contains(req.id)) {
                log.info("Order {} is already processed. Skipping.", req.id);
                return;
            }

            if (req.status.equals("NEW")) {

                // FIX major: Use BigDecimal for monetary calculations to avoid precision loss.
                req.price = req.price.subtract(DISCOUNT_AMOUNT);

                // FIX moderate: The @Transaction annotation should be extracted to a separate service.
                updateOrderStatusInDb(req);

                // FIX moderate: Add to the processed list ONLY after successful execution
                // to avoid inconsistent state if an exception is thrown above.
                processedOrders.add(req.id);
            }
        } catch (Exception e) {
            // FIX major: Log the exception properly and rethrow it to trigger transaction rollback.
            log.error("Error processing order id: {}", req.id, e);
            throw new RuntimeException("Order processing failed", e);
        }
    }

    // FIX: Removed useless @Transactional annotation from the private method.
    private void updateOrderStatusInDb(Order req) {
        log.info("Saving to DB...");
    }
}

class Order {
    protected String id;
    protected String status;
    // FIX major: Changed double to BigDecimal for money.
    protected BigDecimal price;

    // FIX moderate: Encapsulated validation inside the constructor (Always-Valid Domain Model).
    // It is impossible to instantiate an Order in an invalid or partial state.
    public Order(String id, String status, BigDecimal price) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Order price cannot be null");
        }
        this.id = id;
        this.status = status;
        this.price = price;
    }
}
