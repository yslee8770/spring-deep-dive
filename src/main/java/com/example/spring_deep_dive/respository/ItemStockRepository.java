package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.item.ItemStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {

}
