package ru.practicum.yandex.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.model.EndpointHit;


@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    EndpointHitDto toEndpointDto(EndpointHit endpointHit);

    EndpointHit toEndpointModel(EndpointHitDto endpointHitDto);
}
