package com.example.spring_deep_dive.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemSearchCondition {

    private final String name;
    private final String category;
    private final Integer minPrice;
    private final Integer maxPrice;

    @Builder
    private ItemSearchCondition(
            String name,
            String category,
            Integer minPrice,
            Integer maxPrice
    ) {
        this.name = name;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
