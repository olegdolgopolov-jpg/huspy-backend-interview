package com.huspy.interview.task2_inmemory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    private String id;
    private String userId;
    private BigDecimal amount;
    private LocalDate orderDate;

    public Order() {}

    public Order(String id, String userId, BigDecimal amount, LocalDate orderDate) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.orderDate = orderDate;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public static class OrderBuilder {
        private String id;
        private String userId;
        private BigDecimal amount;
        private LocalDate orderDate;

        OrderBuilder() {}

        public OrderBuilder id(String id) {
            this.id = id;
            return this;
        }

        public OrderBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public OrderBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public OrderBuilder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Order build() {
            return new Order(id, userId, amount, orderDate);
        }
    }
}
