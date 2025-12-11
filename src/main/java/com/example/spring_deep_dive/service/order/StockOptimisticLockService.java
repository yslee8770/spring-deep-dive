package com.example.spring_deep_dive.service.order;

import com.example.spring_deep_dive.domain.item.ItemStock;
import com.example.spring_deep_dive.respository.ItemStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockOptimisticLockService {

    private final ItemStockRepository itemStockRepository;

    /**
     * 낙관적 락 기반 재고 차감
     * - 같은 itemId에 대해 동시에 여러 트랜잭션이 들어오면
     *   @Version 필드로 인해 충돌 시 ObjectOptimisticLockingFailureException 발생
     */
    @Transactional
    public void decreaseStockWithOptimisticLock(Long itemId, int qty) {
        ItemStock stock = itemStockRepository.findByItem_Id(itemId)
                .orElseThrow(() -> new IllegalArgumentException("no stock for itemId=" + itemId));

        log.info("[decreaseStock] loaded stockId={}, qty={}, version={}",
                stock.getId(), stock.getQuantity(), stock.getVersion());

        // 동시성 충돌 재현을 위해 일부러 지연
        sleep(200);

        stock.decrease(qty);

        log.info("[decreaseStock] updated stockId={}, qty={}, version={}",
                stock.getId(), stock.getQuantity(), stock.getVersion());
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