package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.domain.order.OrderLine;
import com.example.spring_deep_dive.exception.NotificationException;
import com.example.spring_deep_dive.exception.OrderValidationException;
import com.example.spring_deep_dive.respository.ItemRepository;
import com.example.spring_deep_dive.respository.MemberRepository;
import com.example.spring_deep_dive.respository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    // ============================================
    // Step1 핵심: REQUIRED 중첩 + self-invocation
    // ============================================

    /**
     * 주문 생성 → 내부에서 decreaseStock() 호출
     * decreaseStock()은 REQUIRES_NEW지만 self-invocation 때문에 프록시 미적용 → TX 동일
     */
    @Transactional  // REQUIRED
    public Long placeOrder(Long memberId, Long itemId, int quantity) {
        logTx("placeOrder");

        // 1) Member + Item 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow();

        Item item = itemRepository.findById(itemId)
                .orElseThrow();

        // 2) Order + OrderLine 생성
        Order order = Order.create(member, 0);
        OrderLine line = OrderLine.create(order, item, quantity);
        order.updateTotalAmount(line.calculateAmount());
        orderRepository.save(order);

        // 3) 내부 호출 (self-invocation) → REQUIRES_NEW 무시됨
        decreaseStock(itemId, quantity);

        // 4) 강제 실패 → 전체 롤백 확인
        throw new RuntimeException("Order failed (TX test)");

        // return order.getId();
    }

    /**
     * REQUIRES_NEW인데 self-invocation에서는 절대 새 트랜잭션 생성 안 됨.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseStock(Long itemId, int quantity) {
        logTx("decreaseStock (REQUIRES_NEW 기대)");

        Item item = itemRepository.findById(itemId)
                .orElseThrow();

        item.getStock().decrease(quantity);

        log.info("Stock decreased {} → {}", item.getName(), quantity);
    }

    // ============================================
    // TX 로그 출력
    // ============================================

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, txName={}", point, active, name);
    }

    @Transactional
    public Long placeOrder_step2(Long memberId, Long itemId, int quantity) {
        logTx("placeOrder_step2");

        Member member = memberRepository.findById(memberId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();

        // 주문 생성
        Order order = Order.create(member, 0);
        OrderLine line = OrderLine.create(order, item, quantity);
        order.updateTotalAmount(line.calculateAmount());
        orderRepository.save(order);

        paymentService.savePaymentLog(order.getId(), order.getTotalAmount());

        throw new RuntimeException("Order failed (Step2 TX test)");

        // return order.getId();
    }

    // ===========================
    // Step4-1: checked 예외 기본 동작 (rollback 안 됨)
    // ===========================
    @Transactional
    public void placeOrder_checked_noRollback(Long memberId, Long itemId, int quantity)
            throws OrderValidationException {

        logTx("placeOrder_checked_noRollback");

        Member member = memberRepository.findById(memberId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();

        Order order = Order.create(member, 0);
        OrderLine line = OrderLine.create(order, item, quantity);
        order.updateTotalAmount(line.calculateAmount());
        orderRepository.save(order);

        // checked 예외 던짐 → 기본 설정에서는 롤백 안 됨
        throw new OrderValidationException("validation failed (no rollback)");
    }

    // ===========================
    // Step4-2: checked 예외 + rollbackFor → 롤백
    // ===========================
    @Transactional(rollbackFor = OrderValidationException.class)
    public void placeOrder_checked_withRollback(Long memberId, Long itemId, int quantity)
            throws OrderValidationException {

        logTx("placeOrder_checked_withRollback");

        Member member = memberRepository.findById(memberId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();

        Order order = Order.create(member, 0);
        OrderLine line = OrderLine.create(order, item, quantity);
        order.updateTotalAmount(line.calculateAmount());
        orderRepository.save(order);

        // 같은 예외지만 rollbackFor 지정 → 전체 롤백
        throw new OrderValidationException("validation failed (rollback)");
    }

    // ===========================
    // Step4-3: runtime 예외 + noRollbackFor → 커밋 유지
    // ===========================
    @Transactional(noRollbackFor = NotificationException.class)
    public void placeOrder_runtime_noRollbackFor(Long memberId, Long itemId, int quantity) {

        logTx("placeOrder_runtime_noRollbackFor");

        Member member = memberRepository.findById(memberId).orElseThrow();
        Item item = itemRepository.findById(itemId).orElseThrow();

        Order order = Order.create(member, 0);
        OrderLine line = OrderLine.create(order, item, quantity);
        order.updateTotalAmount(line.calculateAmount());
        orderRepository.save(order);

        // 원래 RuntimeException → 기본은 롤백 대상인데,
        // noRollbackFor 때문에 커밋 유지
        throw new NotificationException("notification failed (no rollback)");
    }

    // =========================
    // Step6-1: NOT_SUPPORTED
    // =========================

    /**
     * TX 안에서 호출되는 outer
     */
    @Transactional
    public long outerWithTx_callNotSupported() {
        logTx("outerWithTx_callNotSupported");

        long cnt = countOrders_notSupported(); // 여기서 TX가 잠시 중단되어야 함

        log.info("order count (NOT_SUPPORTED) = {}", cnt);
        return cnt;
    }

    /**
     * NOT_SUPPORTED: 기존 트랜잭션을 일시 중단하고, TX 없이 수행
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public long countOrders_notSupported() {
        logTx("countOrders_notSupported (NOT_SUPPORTED)");
        return orderRepository.count();
    }

    // =========================
    // Step6-2: MANDATORY
    // =========================

    /**
     * MANDATORY: 반드시 이미 시작된 트랜잭션 안에서만 호출 가능
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateOrderStatus_mandatory(Long orderId) {
        logTx("updateOrderStatus_mandatory (MANDATORY)");

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();  // 예시로 취소 처리
        log.info("Order {} cancelled by mandatory TX", orderId);
    }

    /**
     * TX 안에서 MANDATORY 호출 → 정상 동작
     */
    @Transactional
    public void outerWithTx_callMandatory(Long orderId) {
        logTx("outerWithTx_callMandatory");
        updateOrderStatus_mandatory(orderId); // 여기서는 OK
    }

    /**
     * ❌ 트랜잭션 없이 MANDATORY 호출 → IllegalTransactionStateException 발생
     */
    public void callMandatoryWithoutTx(Long orderId) {
        logTx("callMandatoryWithoutTx (NO TX)");
        updateOrderStatus_mandatory(orderId); // 여기서 예외 터져야 함
    }
}
