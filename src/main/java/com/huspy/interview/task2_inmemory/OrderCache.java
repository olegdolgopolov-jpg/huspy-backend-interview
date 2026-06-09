package com.huspy.interview.task2_inmemory;

import java.util.List;

public interface OrderCache {
    void save(Order order);
    List<Order> getByUserId(String userId);
    // BigDecimal getTotalSalesByDate(LocalDate date);
}
