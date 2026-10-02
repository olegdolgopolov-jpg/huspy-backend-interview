package com.huspy.interview.task3_final;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderProcessorImplTest {

    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderProcessorImpl();
    }

    @Test
    void testPromoDiscountPrecision() {
        // Given
        Order order = new Order("1", "user-1", "PROMO", 0.30, System.currentTimeMillis());

        // When
        processor.processAndSave(order);

        List<Order> userOrders = processor.getOrdersByUser("user-1");
        assertEquals(1, userOrders.size());

        // Then
        assertEquals(0.10, userOrders.get(0).price, "Price should be exactly 0.10");
    }
}