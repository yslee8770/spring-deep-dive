package com.example.spring_deep_dive.service.order;


import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.respository.ItemRepository;
import com.example.spring_deep_dive.respository.ItemStockRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class StockPessimisticLockServiceTest {

    @Autowired
    StockPessimisticLockService stockPessimisticLockService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemStockRepository itemStockRepository;

    Long itemId;

    @BeforeEach
    @Transactional
    void setUp() {
        // 재고 10인 아이템 하나 생성
        Item item = Item.createItem("lock-item-pessimistic", 1000, "LOCK", 10);
        item.turnOn();
        itemRepository.save(item);

        ItemStock stock = item.getStock();
        log.info(">>> [init] stock id={}, qty={}, version={}",
                stock.getId(), stock.getQuantity(), stock.getVersion());

        itemId = item.getId();
    }

    @Test
    @DisplayName("비관락 - 동시에 같은 재고를 차감해도 직렬화되어 최종 수량이 일관되게 감소한다")
    void decreaseStock_concurrent_pessimisticLock() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    stockPessimisticLockService.decreaseStockWithPessimisticLock(itemId, 5, "T" + idx);
                } catch (Exception e) {
                    log.info(">>> thread {} got exception: {}", idx, e.getClass().getSimpleName(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ItemStock stock = itemStockRepository.findByItem_Id(itemId).orElseThrow();
        log.info(">>> [result] final qty={}, version={}",
                stock.getQuantity(), stock.getVersion());

        assertThat(stock.getQuantity()).isEqualTo(0);
    }
}