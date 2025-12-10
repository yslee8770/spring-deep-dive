package com.example.spring_deep_dive.respository;

import com.example.spring_deep_dive.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
