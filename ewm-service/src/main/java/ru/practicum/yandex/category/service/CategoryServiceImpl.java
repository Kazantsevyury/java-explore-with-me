package ru.practicum.yandex.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.events.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    @Override
    public Category addCategory(Category category) {
        final Category savedCategory = categoryRepository.save(category);
        log.info("CategoryController, category with id '{}' was saved.", savedCategory.getId());
        return savedCategory;
    }

    @Override
    public Category updateCategory(Long catId, Category updateCategory) {
        final Category foundCategory = getCategory(catId);
        foundCategory.setName(updateCategory.getName());
        final Category updatedCategory = categoryRepository.save(foundCategory);
        log.info("CategoryController, update category with id '{}', new name: '{}'.", catId, updatedCategory.getName());
        return updatedCategory;
    }

    @Override
    public void removeCategoryById(Long catId) {
        getCategory(catId);
        checkIfCategoryHaveAnyEvents(catId);
        categoryRepository.deleteById(catId);
        log.info("CategoryController, deleted category with id '" + catId + "'.");
    }

    private void checkIfCategoryHaveAnyEvents(Long catId) {
        long eventWithSameCategory = eventRepository.countEventsByCategoryId(catId);
        if (eventWithSameCategory > 0) {
            throw new NotAuthorizedException("Category with id '" + catId + "' still have other event attached to it.");
        }
    }

    @Override
    public List<Category> findCategories(Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Page<Category> categories = categoryRepository.findAll(pageRequest);
        log.info("CategoryService find categories from '{}', size '{}'. Found categories: '{}'.", from, size,
                categories.getSize());
        return categories.getContent();
    }

    @Override
    public Category findCategoryById(Long catId) {
        Category category = getCategory(catId);
        log.info("CategoryService category found: " + category);
        return category;
    }

    private Category getCategory(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id '" + catId + "' not found."));
    }
}
