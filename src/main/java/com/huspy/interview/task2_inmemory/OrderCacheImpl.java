package com.huspy.interview.task2_inmemory;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OrderCacheImpl implements OrderCache {

    @Override
    public void save(Order order) {
        // TODO: Candidate to implement data structures and logic here
    }

    @Override
    public List<Order> getByUserId(String userId) {
        // TODO: Candidate to implement data structures and logic here
        return null;
    }
}
