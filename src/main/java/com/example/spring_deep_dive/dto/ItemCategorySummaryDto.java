package com.example.spring_deep_dive.dto;

import lombok.Getter;

@Getter
public class ItemCategorySummaryDto {

    private final String category;
    private final Long count;
    private final Double avgPrice;

    public ItemCategorySummaryDto(String category, Long count, Double avgPrice) {
        this.category = category;
        this.count = count;
        this.avgPrice = avgPrice;
    }
}
