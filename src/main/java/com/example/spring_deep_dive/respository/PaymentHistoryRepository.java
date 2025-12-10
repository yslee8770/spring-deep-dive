package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.domain.order.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
