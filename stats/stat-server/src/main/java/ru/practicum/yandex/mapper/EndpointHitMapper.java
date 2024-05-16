package ru.practicum.yandex.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.model.EndpointHit;


@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHitDto toDto(EndpointHit endpointHit);

    EndpointHit toModel(EndpointHitDto endpointHitDto);
}
