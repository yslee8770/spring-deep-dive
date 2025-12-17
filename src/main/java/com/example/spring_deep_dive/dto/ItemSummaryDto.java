package com.example.spring_deep_dive.dto;

import lombok.Getter;

@Getter
public class ItemSummaryDto {

    private final Long itemId;
    private final String name;
    private final Integer price;
    private final String category;


    public ItemSummaryDto(Long itemId, String name, int price, String category) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.category = category;
    }

}
