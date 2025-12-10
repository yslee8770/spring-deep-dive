package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.*;
import com.example.spring_deep_dive.respository.ItemRepository;
import com.example.spring_deep_dive.respository.ItemStockRepository;
import com.example.spring_deep_dive.respository.MemberRepository;
import com.example.spring_deep_dive.respository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Long placeOrder(Long memberId, Long itemId, int quantity) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("member not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        ItemStock stock = item.getStock();
        stock.decrease(quantity);

        Order order = Order.create(member, item.getPrice() * quantity);
        OrderLine orderLine = OrderLine.create(order, item, quantity);
        order.addOrderLine(orderLine);

        int totalAmount = order.getOrderLines().stream()
                .mapToInt(OrderLine::calculateAmount)
                .sum();
        order.updateTotalAmount(totalAmount);

        orderRepository.save(order);

        return order.getId();
    }
}
