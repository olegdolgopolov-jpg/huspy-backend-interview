package com.huspy.interview.task1_refactoring;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private List<String> processedOrders = new ArrayList<>();

    public void processData(Order req) {
        try {
            if (req.status.equals("NEW")) {

                double p = req.price - 10.50;
                req.price = p;

                processedOrders.add(req.id);

                updateOrderStatusInDb(req);
            }
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    public void updateOrderStatusInDb(Order req) throws OrderUpdateException {
        System.out.println("Saving to DB...");
    }
}

class Order {
    protected String id;
    protected String status;
    protected double price;
}

class OrderUpdateException extends Exception {
    public OrderUpdateException(String message) {
        super(message);
    }
}