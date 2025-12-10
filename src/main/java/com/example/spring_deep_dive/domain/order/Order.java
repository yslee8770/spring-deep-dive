package com.example.spring_deep_dive.domain.order;

import com.example.spring_deep_dive.domain.BaseEntity;
import com.example.spring_deep_dive.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private int totalAmount;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new java.util.ArrayList<>();


    @Builder
    private Order(Member member,
                  OrderStatus orderStatus,
                  PaymentStatus paymentStatus,
                  int totalAmount) {
        this.member = member;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
    }

    public static Order create(Member member, int totalAmount) {
        return Order.builder()
                .member(member)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.NOT_PAID)
                .paymentStatus(PaymentStatus.NO)
                .build();
    }

    public void completePayment() {
        this.orderStatus = OrderStatus.READY;
        this.paymentStatus = PaymentStatus.YES;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void addOrderLine(OrderLine orderLine) {
        this.orderLines.add(orderLine);
        orderLine.setOrder(this);
    }

    public void updateTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}
