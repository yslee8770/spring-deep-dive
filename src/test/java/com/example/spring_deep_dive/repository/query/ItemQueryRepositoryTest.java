package com.example.spring_deep_dive.repository.query;

import com.example.spring_deep_dive.domain.item.Item;
import com.example.spring_deep_dive.dto.ItemCategorySummaryDto;
import com.example.spring_deep_dive.dto.ItemSearchCondition;
import com.example.spring_deep_dive.dto.ItemSummaryDto;
import com.example.spring_deep_dive.repository.ItemRepository;
import com.example.spring_deep_dive.repository.query.ItemQueryRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@Transactional
class ItemQueryRepositoryTest {

    @Autowired ItemRepository itemRepository;
    @Autowired
    ItemQueryRepository itemQueryRepository;

    private Long appleId;
    private Long bananaId;
    private Long ipadId;

    @BeforeEach
    void setUp() {
        Item i1 = Item.createItem("Apple", 1000, "FOOD", 10);
        Item i2 = Item.createItem("Banana", 2000, "FOOD", 5);
        Item i3 = Item.createItem("iPad", 700000, "DIGITAL", 3);

        appleId = itemRepository.save(i1).getId();
        bananaId = itemRepository.save(i2).getId();
        ipadId = itemRepository.save(i3).getId();
    }

    @Test
    void findByPriceGreaterThan() {
        List<Item> result = itemQueryRepository.findByPriceGreaterThan(1500);
        assertThat(result).extracting("name")
                .containsExactlyInAnyOrder("Banana", "iPad");
    }

    @Test
    void findByNameContains() {
        List<Item> result = itemQueryRepository.findByNameContains("ap");
        assertThat(result).extracting("name")
                .containsExactly("Apple");
    }

    @Test
    void findByCategoryAndPriceRange() {
        List<Item> result = itemQueryRepository.findByCategoryAndPriceRange("FOOD", 1500, 5000);
        assertThat(result).extracting("name")
                .containsExactly("Banana");
    }

    @Test
    void search_byName() {
        ItemSearchCondition cond = ItemSearchCondition.builder()
                .name("ap")
                .build();

        List<Item> result = itemQueryRepository.search(cond);

        assertThat(result)
                .extracting("name")
                .containsExactlyInAnyOrder("Apple");
    }

    @Test
    void search_byCategoryAndMinPrice() {
        ItemSearchCondition cond = ItemSearchCondition.builder()
                .category("FOOD")
                .minPrice(1500)
                .build();

        List<Item> result = itemQueryRepository.search(cond);

        assertThat(result)
                .extracting("name")
                .containsExactly("Banana");
    }

    @Test
    void projection_constructor_maps_correctly() {
        List<ItemSummaryDto> result = itemQueryRepository.findItemSummariesByConstructor();

        assertThat(result)
                .extracting("itemId", "name", "price", "category")
                .containsExactlyInAnyOrder(
                        tuple(appleId, "Apple", 1000, "FOOD"),
                        tuple(bananaId, "Banana", 2000, "FOOD"),
                        tuple(ipadId, "iPad", 700000, "DIGITAL")
                );
    }

    @Test
    void group_by_category() {
        List<ItemCategorySummaryDto> result =
                itemQueryRepository.findCategorySummary();

        assertThat(result)
                .extracting("category", "count")
                .containsExactlyInAnyOrder(
                        tuple("FOOD", 2L),
                        tuple("DIGITAL", 1L)
                );
    }

    @Test
    void group_by_category_having() {
        List<ItemCategorySummaryDto> result =
                itemQueryRepository.findCategorySummaryHavingMinCount(2);

        assertThat(result)
                .extracting("category")
                .containsExactly("FOOD");
    }


    @Test
    void items_more_expensive_than_average() {
        List<Item> result =
                itemQueryRepository.findItemsMoreExpensiveThanAverage();

        assertThat(result)
                .extracting("name")
                .containsExactly("iPad");
    }


}
