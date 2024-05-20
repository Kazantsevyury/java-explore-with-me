package ru.practicum.yandex.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.service.CategoryService;

import javax.validation.Valid;

/**
 * API администратора для категорий
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    /**
     * Добавление новой категории. Имя категории должно быть уникальным. Если добавлено успешно, возвращает статус ответа 201.
     *
     * @param categoryDto параметры новой категории
     * @return добавленная категория
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        log.info("Добавление категории '{}'.", categoryDto);
        final Category category = categoryMapper.toModel(categoryDto);
        final Category addedCategory = categoryService.addCategory(category);
        return categoryMapper.toDto(addedCategory);
    }

    /**
     * Обновление категории. Имя категории должно быть уникальным. Если категория не найдена, возвращает статус ответа 404.
     *
     * @param catId             идентификатор категории для обновления
     * @param updateCategoryDto параметры категории для обновления
     * @return обновленная категория
     */
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto updateCategoryDto) {
        log.info("Обновление категории с id '{}', новое имя: '{}'.", catId, updateCategoryDto.getName());
        final Category updateCategory = categoryMapper.toModel(updateCategoryDto);
        final Category updatedCategory = categoryService.updateCategory(catId, updateCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    /**
     * Удаление категории по идентификатору категории. Категория не может быть связана с событиями, в противном случае возвращает статус ответа 409.
     * Если категория успешно удалена, возвращает статус ответа 204.
     *
     * @param catId идентификатор категории для удаления
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable Long catId) {
        log.info("Удаление категории с id '{}'.", catId);
        categoryService.removeCategoryById(catId);
    }
}
