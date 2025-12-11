package com.example.spring_deep_dive.domain.item;

import com.example.spring_deep_dive.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "item",
        indexes = {
                @Index(name = "idx_item_price", columnList = "price"),
                @Index(name = "idx_item_status", columnList = "itemStatus")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private String category;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @OneToOne(mappedBy = "item", fetch = LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ItemStock stock;

    @Builder
    private Item(String name, int price, String category, ItemStatus itemStatus) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.itemStatus = itemStatus;
    }

    public static Item createItem(String name, int price, String category) {
        return createItem(name, price, category, 0);
    }

    public static Item createItem(String name, int price, String category, int quantity) {
        Item item = Item.builder()
                .name(name)
                .price(price)
                .category(category)
                .itemStatus(ItemStatus.OFF)
                .build();

        ItemStock stock = ItemStock.create(item, quantity);
        item.assignStock(stock);

        return item;
    }

    public void assignStock(ItemStock stock) {
        this.stock = stock;
    }

    public void changePrice(int price) {
        this.price = price;
    }

    public void turnOn() {
        this.itemStatus = ItemStatus.ON;
    }

    public void turnOff() {
        this.itemStatus = ItemStatus.OFF;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
