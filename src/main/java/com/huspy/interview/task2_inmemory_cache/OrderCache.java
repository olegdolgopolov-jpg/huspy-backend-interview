package com.huspy.interview.task2_inmemory_cache;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderCache {
    void save(Order order);
    List<Order> getByUserId(String userId);
    // BigDecimal getTotalSalesByDate(LocalDate date);
    // BigDecimal getTotalSalesByDateRange(LocalDate dateFrom, LocalDate dateTo);
}

class Order {
    protected String id;
    protected String userId;
    protected BigDecimal amount;
    protected LocalDate orderDate;
}