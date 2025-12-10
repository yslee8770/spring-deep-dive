package com.example.spring_deep_dive.domain.item;

import com.example.spring_deep_dive.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 테스트를위해 엔티티 별도 분리
 */

@Entity
@Table(name = "item_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ItemStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_stock_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int quantity;

    @Version
    private int version;

    @Builder
    private ItemStock(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public static ItemStock create(Item item, int quantity) {
        ItemStock stock = ItemStock.builder()
                .item(item)
                .quantity(quantity)
                .build();

        item.assignStock(stock);

        return stock;
    }

    public void decrease(int quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalStateException("재고 부족");
        }
        this.quantity -= quantity;
    }

    public void increase(int quantity) {
        this.quantity += quantity;
    }
}
