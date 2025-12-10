package com.example.spring_deep_dive.domain.order;


import com.example.spring_deep_dive.domain.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "order_line")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_id")
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "item_id")
    Item item;

    int orderPrice;

    int orderQuantity;


    @Builder
    private OrderLine(Order order, Item item, int orderPrice, int orderQuantity) {
        this.order = order;
        this.item = item;
        this.orderPrice = orderPrice;
        this.orderQuantity = orderQuantity;
    }

    public static OrderLine create(Order order, Item item, int quantity) {
        return OrderLine.builder()
                .order(order)
                .item(item)
                .orderPrice(item.getPrice())
                .orderQuantity(quantity)
                .build();
    }
    public int calculateAmount() {
        return this.orderPrice * this.orderQuantity;
    }

    void setOrder(Order order) {
        this.order = order;
    }
}
