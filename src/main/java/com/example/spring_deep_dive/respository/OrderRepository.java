package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
