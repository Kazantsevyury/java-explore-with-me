package ru.practicum.yandex.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Public (for all users) API for categories
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryPublicController {

    private final CategoryService categoryService;

    private final CategoryMapper categoryMapper;

    /**
     * Find categories by page. If nothing was found, returns empty list.
     *
     * @param from first element to display
     * @param size number of elements to display
     * @return found categories
     */
    @GetMapping
    public List<CategoryDto> findCategories(@RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Finding categories from = '{}', size = '{}'.", from, size);
        List<Category> categories = categoryService.findCategories(from, size);
        return categoryMapper.toDtoList(categories);
    }

    /**
     * Find category by category id. If nothing was found, returns 404 response status.
     *
     * @param catId category id to find
     * @return found category
     */
    @GetMapping("/{catId}")
    public CategoryDto findCategoryById(@PathVariable Long catId) {
        log.info("Finding category by id '{}'.", catId);
        Category category = categoryService.findCategoryById(catId);
        return categoryMapper.toDto(category);
    }
}
