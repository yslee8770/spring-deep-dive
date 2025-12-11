package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        select distinct o
        from Order o
        join fetch o.member
        join fetch o.orderLines ol
        join fetch ol.item i
        """)
    List<Order> findAllWithMemberAndLinesAndItemsFetchJoin();
}
