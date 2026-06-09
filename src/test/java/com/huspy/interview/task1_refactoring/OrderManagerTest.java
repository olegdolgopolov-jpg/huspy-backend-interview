package com.huspy.interview.task1_refactoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderManagerTest {

    @Test
    public void testProcessData_NewOrderStatus() {
        OrderManager orderManager = new OrderManager();
        Order order = new Order();
        order.setId("123");
        order.setStatus("NEW");
        order.setPrice(100.0);

        orderManager.processData(order);

        assertEquals(89.5, order.getPrice(), 0.001);
    }

    @Test
    public void testProcessData_OtherStatus() {
        OrderManager orderManager = new OrderManager();
        Order order = new Order();
        order.setId("456");
        order.setStatus("COMPLETED");
        order.setPrice(100.0);

        orderManager.processData(order);

        assertEquals(100.0, order.getPrice(), 0.001);
    }
}
