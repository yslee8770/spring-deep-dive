package com.example.spring_deep_dive.service.order;

import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.repository.ItemStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPessimisticLockService {

    private final ItemStockRepository itemStockRepository;

    /**
     * 비관적 락 기반 재고 차감
     * - SELECT 시점에 PESSIMISTIC_WRITE(for update)로 row 락을 잡음
     * - 동시에 같은 row를 잡으러 오면, 뒤에 온 트랜잭션은 락이 풀릴 때까지 대기
     */
    @Transactional
    public void decreaseStockWithPessimisticLock(Long itemId, int qty, String label) {
        log.info("[{}] try load stock with PESSIMISTIC_WRITE", label);

        ItemStock stock = itemStockRepository.findByItemIdForUpdate(itemId)
                .orElseThrow(() -> new IllegalArgumentException("no stock for itemId=" + itemId));

        log.info("[{}] loaded stockId={}, qty={}, version={}",
                label, stock.getId(), stock.getQuantity(), stock.getVersion());

        sleep(200); // 동시성 재현용 딜레이

        stock.decrease(qty);

        log.info("[{}] updated stockId={}, qty={}, version={}",
                label, stock.getId(), stock.getQuantity(), stock.getVersion());
        // commit 시점에 UPDATE + 락 해제
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
