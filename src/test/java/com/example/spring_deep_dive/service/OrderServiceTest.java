package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.domain.order.PaymentHistory;
import com.example.spring_deep_dive.exception.NotificationException;
import com.example.spring_deep_dive.exception.OrderValidationException;
import com.example.spring_deep_dive.respository.ItemRepository;
import com.example.spring_deep_dive.respository.MemberRepository;
import com.example.spring_deep_dive.respository.OrderRepository;
import com.example.spring_deep_dive.respository.PaymentHistoryRepository;
import com.example.spring_deep_dive.service.order.OrderService;
import com.example.spring_deep_dive.service.order.OrderServiceFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.IllegalTransactionStateException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderServiceFacade orderServiceFacade;

    @Autowired
    PaymentHistoryRepository paymentHistoryRepository;


    @BeforeEach
    void cleanUp() {
        paymentHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void step1_selfInvocation_requiresNew_ignored() {
        Member member = memberRepository.save(Member.createUser("a@a.com", "A","TEST"));
        Item item = itemRepository.save(Item.createItem("Book", 10000, "BOOK"));

        try {
            orderService.placeOrder(member.getId(), item.getId(), 2);
        } catch (Exception ignored) {}

        ItemStock stock = itemRepository.findById(item.getId())
                .orElseThrow()
                .getStock();

        // 전체 롤백되어 재고는 원래대로 유지
        assertThat(stock.getQuantity()).isEqualTo(0);
    }

    @Test
    void step2_requiresNew_works() {
        Member member = memberRepository.save(Member.createUser("a@a.com", "A","TEST"));
        Item item = itemRepository.save(Item.createItem("Book", 10000, "BOOK"));

        try {
            orderService.placeOrder_step2(member.getId(), item.getId(), 2);
        } catch (Exception ignored) {}

        // 1) 주문은 outer rollback → DB에 없음
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty();

        // 2) REQUIRES_NEW는 commit 됨 → payment log는 DB에 남아야 함
        List<PaymentHistory> logs = paymentHistoryRepository.findAll();
        assertThat(logs).hasSize(1);

        log.info("Payment log saved = {}", logs.get(0).getId());
    }

    @Test
    @DisplayName("Step4-1: checked 예외는 기본 설정에서는 롤백되지 않는다")
    void checkedException_default_noRollback() throws Exception {
        Member m = newMember("chk1@test.com", "CHK1");
        Item item = newItem("Book", 10000);

        try {
            orderService.placeOrder_checked_noRollback(m.getId(), item.getId(), 1);
        } catch (OrderValidationException e) {
            log.info("caught checked exception: {}", e.getMessage());
        }

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1); // ✔ 커밋됨
    }

    @Test
    @DisplayName("Step4-2: rollbackFor로 checked 예외도 롤백 대상으로 만든다")
    void checkedException_withRollbackFor() throws Exception {
        Member m = newMember("chk2@test.com", "CHK2");
        Item item = newItem("Book", 10000);

        try {
            orderService.placeOrder_checked_withRollback(m.getId(), item.getId(), 1);
        } catch (OrderValidationException e) {
            log.info("caught checked exception: {}", e.getMessage());
        }

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty(); // ✔ 롤백됨
    }

    @Test
    @DisplayName("Step4-3: runtime 예외도 noRollbackFor로 커밋 유지 가능")
    void runtimeException_noRollbackFor() {
        Member m = newMember("rt@test.com", "RT");
        Item item = newItem("Book", 10000);

        try {
            orderService.placeOrder_runtime_noRollbackFor(m.getId(), item.getId(), 1);
        } catch (NotificationException e) {
            log.info("caught runtime exception: {}", e.getMessage());
        }

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1); // ✔ 커밋 유지
    }

    @Test
    @DisplayName("Step6-1: NOT_SUPPORTED는 기존 트랜잭션을 일시 중단하고 TX 없이 실행된다")
    void notSupported_suspends_existing_tx() {
        Member m = newMember("ns@test.com", "NS");
        Item item = newItem("Book", 10000);
        newOrder(m, item);
        newOrder(m, item);

        long count = orderService.outerWithTx_callNotSupported();

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Step6-2_1: MANDATORY는 트랜잭션 없으면 예외 발생_self-invocation 로 정상작동")
    void mandatory_without_tx_throws_exception_1() {
        Member m = newMember("man1@test.com", "MAN1");
        Item item = newItem("Book", 10000);
        Order order = newOrder(m, item);
        orderService.callMandatoryWithoutTx(order.getId());
    }

    @Test
    @DisplayName("Step6-2_2: MANDATORY는 트랜잭션 없으면 예외 발생")
    void mandatory_without_tx_throws_exception_2() {
        Member m = newMember("man1@test.com", "MAN1");
        Item item = newItem("Book", 10000);
        Order order = newOrder(m, item);

        assertThatThrownBy(() -> orderServiceFacade.callMandatoryWithoutTx(order.getId()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    @DisplayName("Step6-3: MANDATORY는 이미 시작된 TX 안에서는 정상 동작")
    void mandatory_with_existing_tx_works() {
        Member m = newMember("man2@test.com", "MAN2");
        Item item = newItem("Book", 10000);
        Order order = newOrder(m, item);

        orderService.outerWithTx_callMandatory(order.getId());

        Order found = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(found.getOrderStatus()).isEqualTo(
                com.example.spring_deep_dive.domain.order.OrderStatus.CANCELLED
        );
    }

    private Member newMember(String email, String name) {
        return memberRepository.save(Member.createUser(email,"test" ,name));
    }

    private Item newItem(String name, int price) {
        return itemRepository.save(Item.createItem(name, price, "CAT"));
    }

    private Order newOrder(Member m, Item item) {
        Order order = Order.create(m, 0);
        order.addOrderLine(
                com.example.spring_deep_dive.domain.order.OrderLine.create(order, item, 1)
        );
        order.updateTotalAmount(item.getPrice());
        return orderRepository.save(order);
    }
}