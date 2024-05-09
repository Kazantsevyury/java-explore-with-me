package ru.practicum.yandex.category.service;

import ru.practicum.yandex.category.model.Category;

import java.util.List;

public interface CategoryService {
    Category addCategory(Category category);

    Category updateCategory(Long catId, Category updateCategory);

    void removeCategoryById(Long catId);

    List<Category> findCategories(Long from, Integer size);

    Category findCategoryById(Long catId);
}
