package solution;


import com.huspy.interview.task3_final.solution.OrderProcessor;
import com.huspy.interview.task3_final.solution.OrderProcessorImpl;
import com.huspy.interview.task3_final.solution.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorImplTest {

    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderProcessorImpl();
    }

    @Test
    void testPromoDiscountPrecision() {
        // Given: Order with price 0.30
        Order order = new Order("1", "user-1", "PROMO", new BigDecimal("0.30"), System.currentTimeMillis());

        // When
        processor.processAndSave(order);

        // Then: Price should be exactly 0.10 (0.30 - 0.20)
        List<Order> userOrders = processor.getOrdersByUser("user-1");
        assertEquals(1, userOrders.size());

        // This will pass smoothly with BigDecimal, but would fail with double (0.09999999999999998)
        assertEquals(new BigDecimal("0.10"), userOrders.get(0).price(), "Price should be exactly 0.10");
    }

    @Test
    void testDeduplication() {
        // Given: Two orders with the exact same ID
        Order order1 = new Order("123", "user-1", "NEW", new BigDecimal("100.00"), System.currentTimeMillis());
        Order order2 = new Order("123", "user-1", "NEW", new BigDecimal("100.00"), System.currentTimeMillis());

        // When
        processor.processAndSave(order1);
        processor.processAndSave(order2);

        // Then: Only one order should be saved
        List<Order> userOrders = processor.getOrdersByUser("user-1");
        assertEquals(1, userOrders.size(), "Duplicate order should be ignored");
    }

    @Test
    void testExceptionPropagationAndStateRollback() {
        // Given: An order that will result in a negative price (validation failure)
        Order badOrder = new Order("bad-id-1", "user-2", "PROMO", new BigDecimal("0.10"), System.currentTimeMillis());

        // When & Then (Part 1): Verify the exception is actually thrown and not swallowed
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> processor.processAndSave(badOrder),
                "Expected processAndSave to throw, but it didn't"
        );
        assertEquals("Price cannot be negative", thrown.getMessage());

        // When & Then (Part 2): Verify State Rollback.
        // If we send a CORRECTED order with the SAME ID, it should NOT be blocked by deduplication.
        Order fixedOrder = new Order("bad-id-1", "user-2", "NEW", new BigDecimal("50.00"), System.currentTimeMillis());

        assertDoesNotThrow(() -> processor.processAndSave(fixedOrder));

        List<Order> userOrders = processor.getOrdersByUser("user-2");
        assertEquals(1, userOrders.size(), "Corrected order should be saved, rollback failed!");
    }

    @Test
    void testUnmodifiableList() {
        // Given
        processor.processAndSave(new Order("1", "user-1", "NEW", new BigDecimal("10.0"), System.currentTimeMillis()));
        List<Order> userOrders = processor.getOrdersByUser("user-1");

        // When & Then: Trying to modify the returned list should throw an exception (protecting the cache)
        assertThrows(UnsupportedOperationException.class, () -> {
            userOrders.add(new Order("2", "user-1", "NEW", new BigDecimal("20.0"), System.currentTimeMillis()));
        });
    }

    /**
     * CONCURRENCY TEST
     * Tests that the cache can handle hundreds of concurrent requests without:
     * 1. Losing data (Race conditions on list initialization).
     * 2. Throwing ConcurrentModificationException.
     * 3. Breaking deduplication.
     */
    @Test
    void testMultithreading() throws InterruptedException {
        int threadsCount = 500;
        ExecutorService executor = Executors.newFixedThreadPool(50);

        // Latch to make all threads start at the exact same moment for maximum contention
        CountDownLatch startLatch = new CountDownLatch(1);
        // Latch to wait for all threads to finish
        CountDownLatch doneLatch = new CountDownLatch(threadsCount);

        AtomicInteger unexpectedExceptions = new AtomicInteger(0);

        for (int i = 0; i < threadsCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait until all threads are ready

                    // Half of the threads will try to save UNIQUE orders
                    // The other half will try to save the EXACT SAME duplicate order
                    if (index % 2 == 0) {
                        // Unique orders for the same user (Tests map and list thread-safety)
                        processor.processAndSave(new Order("unique-" + index, "shared-user", "NEW", new BigDecimal("10.0"), System.currentTimeMillis()));
                    } else {
                        // Duplicate order (Tests concurrent deduplication set)
                        processor.processAndSave(new Order("duplicate-ID", "shared-user", "NEW", new BigDecimal("10.0"), System.currentTimeMillis()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    unexpectedExceptions.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Unleash all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete (timeout after 10 seconds to prevent infinite hangs)
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Threads did not finish in time");
        executor.shutdown();

        // Assertions
        assertEquals(0, unexpectedExceptions.get(), "Unexpected exceptions occurred during concurrent execution");

        List<Order> sharedUserOrders = processor.getOrdersByUser("shared-user");

        // Total expected orders: 250 unique ones + exactly 1 from the duplicate batch = 251
        assertEquals(251, sharedUserOrders.size(), "Concurrency issue: Data loss or duplicate bypass detected");
    }
}