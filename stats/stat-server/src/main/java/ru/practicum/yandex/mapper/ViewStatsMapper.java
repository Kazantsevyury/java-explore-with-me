package ru.practicum.yandex.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.model.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewStatsMapper {

    ViewStatsDto toModel(ViewStats viewStats);

    ViewStats toDto(ViewStatsDto viewStatsDto);

    List<ViewStatsDto> toDtoList(List<ViewStats> viewStatsList);
}
