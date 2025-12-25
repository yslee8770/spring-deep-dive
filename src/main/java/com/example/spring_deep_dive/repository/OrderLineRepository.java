package com.example.spring_deep_dive.repository;

import com.example.spring_deep_dive.domain.order.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

}
