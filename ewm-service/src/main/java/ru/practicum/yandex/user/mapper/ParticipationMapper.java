package ru.practicum.yandex.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.yandex.user.dto.ParticipationRequestDto;
import ru.practicum.yandex.user.model.ParticipationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationMapper {

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> participationRequests);
}
