package ru.practicum.yandex.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Общедоступный (для всех пользователей) API для категорий
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    /**
     * Поиск категорий по странице. Если ничего не найдено, возвращает пустой список.
     *
     * @param from первый элемент для отображения
     * @param size количество элементов для отображения
     * @return найденные категории
     */
    @GetMapping
    public List<CategoryDto> findCategories(@RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Поиск категорий от = '{}', размер = '{}'.", from, size);
        List<Category> categories = categoryService.findCategories(from, size);
        return categoryMapper.toDtoList(categories);
    }

    /**
     * Поиск категории по идентификатору категории. Если ничего не найдено, возвращает статус ответа 404.
     *
     * @param catId идентификатор категории для поиска
     * @return найденная категория
     */
    @GetMapping("/{catId}")
    public CategoryDto findCategoryById(@PathVariable Long catId) {
        log.info("Поиск категории по id '{}'.", catId);
        Category category = categoryService.findCategoryById(catId);
        return categoryMapper.toDto(category);
    }
}
