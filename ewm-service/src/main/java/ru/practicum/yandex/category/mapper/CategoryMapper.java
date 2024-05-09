package ru.practicum.yandex.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.category.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    Category toModel(CategoryDto categoryDto);

    List<CategoryDto> toDtoList(List<Category> categories);
}
