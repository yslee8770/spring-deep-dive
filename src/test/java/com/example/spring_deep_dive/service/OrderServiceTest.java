package com.example.spring_deep_dive.service;


import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.respository.ItemRepository;
import com.example.spring_deep_dive.respository.ItemStockRepository;
import com.example.spring_deep_dive.respository.MemberRepository;
import com.example.spring_deep_dive.respository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceBasicTest {

    @Autowired OrderService orderService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ItemStockRepository itemStockRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    @Transactional
    @DisplayName("기본 주문 생성 플로우가 동작하는지 확인")
    void placeOrder_basic_flow() {
        // given
        Member member = Member.createUser("test@test.com", "test","test");
        memberRepository.save(member);

        Item item = Item.createItem("JPA 스터디권", 10000, "STUDY");
        itemRepository.save(item);

        ItemStock stock = ItemStock.create(item, 10);
        itemStockRepository.save(stock);

        // when
        Long orderId = orderService.placeOrder(member.getId(), item.getId(), 3);

        // then
        Order order = orderRepository.findById(orderId).orElseThrow();

        assertThat(order.getTotalAmount()).isEqualTo(30000);
        assertThat(stock.getQuantity()).isEqualTo(7);
        assertThat(order.getOrderLines()).hasSize(1);
    }
}