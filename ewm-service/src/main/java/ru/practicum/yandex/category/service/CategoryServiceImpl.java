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
     * Добавление новой категории. Имя категории должно быть уникальным.
     *
     * @param category параметры новой категории
     * @return добавленная категория
     */
    @Override
    @Transactional
    public Category addCategory(Category category) {
        final Category savedCategory = categoryRepository.save(category);
        log.info("CategoryController, категория с id '{}' была сохранена.", savedCategory.getId());
        return savedCategory;
    }

    /**
     * Обновление категории. Имя категории должно быть уникальным. Если категория с catId не найдена, генерирует NotFoundException.
     *
     * @param catId          идентификатор категории для обновления
     * @param updateCategory параметры категории для обновления
     * @return обновленная категория
     */
    @Override
    @Transactional
    public Category updateCategory(Long catId, Category updateCategory) {
        final Category foundCategory = getCategory(catId);
        foundCategory.setName(updateCategory.getName());
        final Category updatedCategory = categoryRepository.save(foundCategory);
        log.info("CategoryController, обновление категории с id '{}', новое имя: '{}'.", catId, updatedCategory.getName());
        return updatedCategory;
    }

    /**
     * Удаление категории по идентификатору категории. Категория не может быть связана с событиями, в противном случае генерирует NotAuthorizedException.
     *
     * @param catId идентификатор категории для удаления
     */
    @Override
    @Transactional
    public void removeCategoryById(Long catId) {
        getCategory(catId);
        checkIfCategoryHaveAnyEvents(catId);
        categoryRepository.deleteById(catId);
        log.info("CategoryController, удалена категория с id '" + catId + "'.");
    }

    /**
     * Поиск категорий по странице. Если ничего не найдено, возвращает пустой список.
     *
     * @param from первый элемент для отображения
     * @param size количество элементов для отображения
     * @return найденные категории
     */
    @Override
    public List<Category> findCategories(Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Page<Category> categories = categoryRepository.findAll(pageRequest);
        log.info("CategoryService поиск категорий от '{}', размер '{}'. Найдено категорий: '{}'.", from, size,
                categories.getSize());
        return categories.getContent();
    }

    /**
     * Поиск категории по идентификатору категории. Если ничего не найдено, генерирует NotFoundException.
     *
     * @param catId идентификатор категории для поиска
     * @return найденная категория
     */
    @Override
    public Category findCategoryById(Long catId) {
        Category category = getCategory(catId);
        log.info("CategoryService категория найдена: " + category);
        return category;
    }

    private Category getCategory(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id '" + catId + "' не найдена."));
    }

    private void checkIfCategoryHaveAnyEvents(Long catId) {
        long eventWithSameCategory = eventRepository.countEventsByCategoryId(catId);
        if (eventWithSameCategory > 0) {
            throw new NotAuthorizedException("Категория с id '" + catId + "' все еще имеет другое событие, прикрепленное к ней.");
        }
    }
}
