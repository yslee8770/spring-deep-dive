package com.example.spring_deep_dive.domain.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private int amount;
    private String message;

    @Builder
    private PaymentHistory(Long orderId, int amount, String message) {
        this.orderId = orderId;
        this.amount = amount;
        this.message = message;
    }

    public static PaymentHistory create(Long orderId, int amount, String message) {
        return PaymentHistory.builder()
                .orderId(orderId)
                .amount(amount)
                .message(message)
                .build();
    }
}
