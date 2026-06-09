package com.huspy.interview.task1_refactoring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderManager {

    // Intentional mistake: mutable state in a singleton (not thread-safe)
    private List<String> processedOrders = new ArrayList<>();

    public void processData(Order req) {
        try {
            // Intentional mistake: potential NPE if req.getStatus() is null
            if (req.getStatus().equals("NEW")) {

                // Intentional mistakes: magic number (10.50) and double arithmetic
                double p = req.getPrice() - 10.50;
                req.setPrice(p);

                processedOrders.add(req.getId());

                // Intentional mistake: self-invocation, Spring proxy is bypassed, transaction won't work
                this.updateOrderStatusInDb(req);
                sendEmail(req.getId());
            }
        } catch (Exception e) {
            // Intentional mistake: swallowing the exception and using System.out
            System.out.println("error");
        }
    }

    // Intentional mistake: @Transactional on a private method (Spring AOP ignores it)
    @Transactional
    private void updateOrderStatusInDb(Order req) {
        // Emulating saving updated status and price to DB
        System.out.println("Saving to DB...");
    }

    private void sendEmail(String userId) {
        // Emulating email sending
        System.out.println("Sending email to " + userId);
    }
}
