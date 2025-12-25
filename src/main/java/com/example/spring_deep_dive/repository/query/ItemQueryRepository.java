package com.example.spring_deep_dive.repository.query;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.domain.item.QItem;
import com.example.spring_deep_dive.dto.ItemCategorySummaryDto;
import com.example.spring_deep_dive.dto.ItemSearchCondition;
import com.example.spring_deep_dive.dto.ItemSummaryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.spring_deep_dive.domain.item.QItem.*;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 가격이 특정 값 초과인 Item 목록 조회
     */
    public List<Item> findByPriceGreaterThan(int price) {
        return queryFactory
                .selectFrom(item)
                .where(item.price.gt(price))
                .fetch();
    }

    /**
     * 이름에 키워드가 포함된 Item 목록 조회 (부분 검색)
     */
    public List<Item> findByNameContains(String keyword) {
        return queryFactory
                .selectFrom(item)
                .where(item.name.containsIgnoreCase(keyword))
                .fetch();
    }
    /**
     * 카테고리 + 가격 범위 검색
     */
    public List<Item> findByCategoryAndPriceRange(String category, int minPrice, int maxPrice) {
        return queryFactory
                .selectFrom(item)
                .where(
                        item.category.eq(category),
                        item.price.between(minPrice,maxPrice)
                )
                .fetch();
    }

    /**
     * Chapter 2: 동적 검색 (BooleanBuilder)
     */
    public List<Item> search(ItemSearchCondition condition) {
        return queryFactory
                .selectFrom(item)
                .where(
                        nameContains(condition.getName()),
                        categoryEq(condition.getCategory()),
                        minPriceGoe(condition.getMinPrice()),
                        maxPriceLoe(condition.getMaxPrice())
                )
                .fetch();
    }



 // Constructor
    public List<ItemSummaryDto> findItemSummariesByConstructor() {
        return queryFactory
                .select(Projections.constructor(
                        ItemSummaryDto.class,
                        item.id,
                        item.name,
                        item.price,
                        item.category
                ))
                .from(item)
                .fetch();
    }

    public List<ItemCategorySummaryDto> findCategorySummary() {
        return queryFactory
                .select(Projections.constructor(
                        ItemCategorySummaryDto.class,
                        item.category,
                        item.count(),
                        item.price.avg()
                ))
                .from(item)
                .groupBy(item.category)
                .fetch();
    }

    public List<ItemCategorySummaryDto> findCategorySummaryHavingMinCount(long minCount) {
        return queryFactory
                .select(Projections.constructor(
                        ItemCategorySummaryDto.class,
                        item.category,
                        item.count(),
                        item.price.avg()
                ))
                .from(item)
                .groupBy(item.category)
                .having(item.count().goe(minCount))
                .fetch();
    }

    public List<Item> findItemsMoreExpensiveThanAverage() {
        QItem itemSub = new QItem("itemSub");

        return queryFactory
                .selectFrom(item)
                .where(item.price.gt(
                        JPAExpressions
                                .select(itemSub.price.avg())
                                .from(itemSub)
                ))
                .fetch();
    }




    private BooleanExpression nameContains(String name) {
        return hasText(name) ? item.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression categoryEq(String category) {
        return hasText(category) ? item.category.eq(category) : null;
    }

    private BooleanExpression minPriceGoe(Integer minPrice) {
        return minPrice != null ? item.price.goe(minPrice) : null;
    }

    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        return maxPrice != null ? item.price.loe(maxPrice) : null;
    }
}