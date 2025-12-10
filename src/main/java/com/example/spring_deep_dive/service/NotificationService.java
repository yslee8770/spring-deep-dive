package com.example.spring_deep_dive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationService {

    @Transactional
    public void sendOrderCompleted(Long orderId) {
        log.info("send order completed notification. orderId={}", orderId);
    }
}
