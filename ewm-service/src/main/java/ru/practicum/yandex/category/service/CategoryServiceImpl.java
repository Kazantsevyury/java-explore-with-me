package ru.practicum.yandex.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    /**
     * Add new category. Category name must be unique.
     *
     * @param category new category parameters
     * @return added category
     */
    @Override
    @Transactional
    public Category addCategory(Category category) {
        final Category savedCategory = categoryRepository.save(category);
        log.info("CategoryController, category with id '{}' was saved.", savedCategory.getId());
        return savedCategory;
    }

    /**
     * Update category. Category name must be unique. If category with catId not found, throws NotFoundException.
     *
     * @param catId          category id to update
     * @param updateCategory category parameters to update
     * @return updated category
     */
    @Override
    @Transactional
    public Category updateCategory(Long catId, Category updateCategory) {
        final Category foundCategory = getCategory(catId);
        foundCategory.setName(updateCategory.getName());
        final Category updatedCategory = categoryRepository.save(foundCategory);
        log.info("CategoryController, update category with id '{}', new name: '{}'.", catId, updatedCategory.getName());
        return updatedCategory;
    }

    /**
     * Delete category by category id. Category can not be linked to any events, otherwise throws NotAuthorizedException.
     *
     * @param catId category id to delete
     */
    @Override
    @Transactional
    public void removeCategoryById(Long catId) {
        getCategory(catId);
        checkIfCategoryHaveAnyEvents(catId);
        categoryRepository.deleteById(catId);
        log.info("CategoryController, deleted category with id '" + catId + "'.");
    }

    /**
     * Find categories by page. If nothing was found, returns empty list.
     *
     * @param from first element to display
     * @param size number of elements to display
     * @return found categories
     */
    @Override
    public List<Category> findCategories(Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Page<Category> categories = categoryRepository.findAll(pageRequest);
        log.info("CategoryService find categories from '{}', size '{}'. Found categories: '{}'.", from, size,
                categories.getSize());
        return categories.getContent();
    }

    /**
     * Find category by category id. If nothing was found, throws NotFoundException.
     *
     * @param catId category id to find
     * @return found category
     */
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

    private void checkIfCategoryHaveAnyEvents(Long catId) {
        long eventWithSameCategory = eventRepository.countEventsByCategoryId(catId);
        if (eventWithSameCategory > 0) {
            throw new NotAuthorizedException("Category with id '" + catId + "' still have other event attached to it.");
        }
    }
}
