package ru.practicum.yandex.events.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.events.dto.LocationDto;
import ru.practicum.yandex.events.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toModel(LocationDto locationDto);

    LocationDto toDto(Location location);
}
