package com.example.spring_deep_dive.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceFacade {

    @Autowired
    private OrderService orderService;

    public void callMandatoryWithoutTx(Long orderId) {
        orderService.updateOrderStatus_mandatory(orderId);
    }
}