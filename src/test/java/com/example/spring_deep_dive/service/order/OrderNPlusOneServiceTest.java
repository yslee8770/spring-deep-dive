package com.example.spring_deep_dive.service.order;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.domain.order.OrderLine;
import com.example.spring_deep_dive.repository.ItemRepository;
import com.example.spring_deep_dive.repository.MemberRepository;
import com.example.spring_deep_dive.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
class OrderNPlusOneServiceTest {

    @Autowired
    OrderService service;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        log.info(">>> N+1 테스트용 데이터 세팅 시작");

        Member user1 = Member.createUser("user1@test.com", "encoded", "user1");
        Member user2 = Member.createUser("user2@test.com", "encoded", "user2");
        memberRepository.save(user1);
        memberRepository.save(user2);

        Item itemA = createItem("itemA", 1000, "CAT_A");
        Item itemB = createItem("itemB", 2000, "CAT_B");
        Item itemC = createItem("itemC", 3000, "CAT_C");

        Order o1 = Order.create(user1, 3000);
        o1.addOrderLine(OrderLine.create(o1, itemA, 1));
        o1.addOrderLine(OrderLine.create(o1, itemB, 1));

        Order o2 = Order.create(user1, 5000);
        o2.addOrderLine(OrderLine.create(o2, itemA, 2));
        o2.addOrderLine(OrderLine.create(o2, itemC, 1));

        Order o3 = Order.create(user2, 3000);
        o3.addOrderLine(OrderLine.create(o3, itemB, 1));
        o3.addOrderLine(OrderLine.create(o3, itemC, 1));

        orderRepository.save(o1);
        orderRepository.save(o2);
        orderRepository.save(o3);

        log.info(">>> N+1 테스트용 데이터 세팅 완료");
    }

    private Item createItem(String name, int price, String category) {
        Item item = Item.createItem(name, price, category, 100);
        item.turnOn();
        return itemRepository.save(item);
    }

    @Test
    @DisplayName("N+1 재현 - 기본 LAZY 조회로 주문 목록 조회")
    void nPlusOne_reproduce() {
        service.loadOrdersWithMemberAndLinesAndItems_NPlusOne();
        // 콘솔에서 기대하는 SQL 패턴:
        // 1) select * from orders ...
        // 2) 각 order마다 member 조회 (N번)
        // 3) 각 order마다 order_line 조회 (N번)
        // 4) 각 order_line마다 item 조회 (M번)
        // → 쿼리 개수가 데이터 개수에 비례해서 비정상적으로 증가함.
    }

    @Test
    @DisplayName("fetch join으로 주문/회원/주문라인/아이템 한 번에 로딩 (N+1 제거)")
    void fetchJoin_removeNPlusOne() {
        service.loadOrdersWithFetchJoin();

        // 콘솔에서:
        // - join fetch 로 orders + member + orderLines + item 한 방 쿼리 1개
    }

    @Test
    @DisplayName("batch_size로 N+1 완화 - IN 쿼리로 묶어서 조회")
    void batchSize_optimizeNPlusOne() {
        service.loadOrdersWithBatchSize();

        // 기대 패턴 (대략적인 감각만 보면 됨):
        // - orders 조회: select ... from orders
        // - member / order_line / item 조회가
        //   "id in (?, ?, ?, ...)" 형태의 IN 쿼리로 묶여서 나옴
        // - N+1 때처럼 주문 수/라인 수만큼 개별 select가 터지지 않고,
        //   몇 번 안 되는 IN 쿼리로 줄어든 걸 로그로 확인
    }
}