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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class StockOptimisticLockServiceTest {

    @Autowired
    StockOptimisticLockService stockOptimisticLockService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemStockRepository itemStockRepository;

    Long itemId;

    @BeforeEach
    @Transactional
    void setUp() {
        // 재고 10인 아이템 하나 생성
        Item item = Item.createItem("lock-item", 1000, "LOCK", 10);
        item.turnOn();
        itemRepository.save(item);

        ItemStock stock = item.getStock();
        log.info(">>> 초기 stock id={}, qty={}, version={}",
                stock.getId(), stock.getQuantity(), stock.getVersion());

        itemId = item.getId();
    }

    @Test
    @DisplayName("단일 스레드에서 낙관락 기반 재고 차감 - 정상 동작")
    void decreaseStock_singleThread() {
        stockOptimisticLockService.decreaseStockWithOptimisticLock(itemId, 3);
        stockOptimisticLockService.decreaseStockWithOptimisticLock(itemId, 2);

        ItemStock stock = itemStockRepository.findByItem_Id(itemId).orElseThrow();
        log.info(">>> 최종 qty={}, version={}", stock.getQuantity(), stock.getVersion());

        assertThat(stock.getQuantity()).isEqualTo(10 - 3 - 2);
        assertThat(stock.getVersion()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("동시에 같은 재고를 차감하면 낙관락 예외 발생")
    void decreaseStock_concurrent_optimisticLockFailure() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 예외 하나는 반드시 발생해야 함
        final Exception[] exHolder = new Exception[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    stockOptimisticLockService.decreaseStockWithOptimisticLock(itemId, 5);
                } catch (Exception e) {
                    log.info(">>> thread {} got exception: {}", idx, e.getClass().getSimpleName());
                    exHolder[idx] = e;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 둘 중 하나는 낙관락 예외가 나야 함
        boolean hasOptimisticFailure = false;
        for (Exception ex : exHolder) {
            if (ex instanceof ObjectOptimisticLockingFailureException) {
                hasOptimisticFailure = true;
            }
        }

        assertThat(hasOptimisticFailure)
                .as("동시에 같은 재고를 차감하면 ObjectOptimisticLockingFailureException가 발생해야 한다")
                .isTrue();

        ItemStock stock = itemStockRepository.findByItem_Id(itemId).orElseThrow();
        log.info(">>> 최종 qty={}, version={}", stock.getQuantity(), stock.getVersion());
    }
}