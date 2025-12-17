package com.example.spring_deep_dive.respository.query;

import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.domain.order.OrderStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


import static com.example.spring_deep_dive.domain.item.QItem.item;
import static com.example.spring_deep_dive.domain.member.QMember.member;
import static com.example.spring_deep_dive.domain.order.QOrder.order;
import static com.example.spring_deep_dive.domain.order.QOrderLine.orderLine;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;
    /**
     * 주문 + 회원 조인해서 주문 목록 조회
     * (기본 inner join 예제)
     */
    public List<Order> findOrdersWithMember() {
        return queryFactory
                .selectFrom(order)
                .join(order.member,member)
                .fetch();
    }

    /**
     * 회원 이메일로 주문 검색
     */
    public List<Order> findByMemberEmail(String email) {
        return queryFactory
                .selectFrom(order)
                .join(order.member, member)
                .where(member.email.eq(email))
                .fetch();
    }

    public List<Order> findOrdersWithMemberFetchJoin() {
        return queryFactory
                .selectFrom(order)
                .join(order.member, member).fetchJoin()
                .fetch();
    }

    public List<Long> findOrderIds(Pageable pageable) {
        return queryFactory
                .select(order.id)
                .from(order)
                .orderBy(order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public List<Order> findOrdersWithLines(List<Long> orderIds) {
        return queryFactory
                .selectFrom(order)
                .join(order.orderLines, orderLine).fetchJoin()
                .where(order.id.in(orderIds))
                .orderBy(order.id.desc())
                .fetch();
    }

}
