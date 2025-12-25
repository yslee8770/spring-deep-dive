package com.example.spring_deep_dive.repository;

import com.example.spring_deep_dive.domain.item.ItemStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {

    Optional<ItemStock> findByItem_Id(Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s
        from ItemStock s
        where s.item.id = :itemId
        """)
    Optional<ItemStock> findByItemIdForUpdate(Long itemId);
}


