package com.huspy.interview.task1_refactoring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
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

    @Transactional
    public void updateOrderStatusInDb(Order req) {
        System.out.println("Saving to DB...");
    }
}

class Order {
    protected String id;
    protected String status;
    protected double price;
}