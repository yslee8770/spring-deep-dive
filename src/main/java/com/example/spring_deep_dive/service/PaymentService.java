package com.example.spring_deep_dive.service;


import com.example.spring_deep_dive.domain.order.PaymentHistory;
import com.example.spring_deep_dive.respository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePaymentLog(Long orderId, int amount) {
        logTx("savePaymentLog (REQUIRES_NEW)");

        PaymentHistory history = PaymentHistory.create(orderId, amount, "PAYMENT_LOG");
        paymentHistoryRepository.save(history);

        log.info("Payment log saved: order={}, amount={}", orderId, amount);
    }

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, txName={}", point, active, name);
    }
}
