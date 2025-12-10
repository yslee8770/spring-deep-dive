package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.order.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

}
