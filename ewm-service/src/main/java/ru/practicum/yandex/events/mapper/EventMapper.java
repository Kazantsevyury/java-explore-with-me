package ru.practicum.yandex.events.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventShortDto;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.mapper.UserMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, UserMapper.class, CategoryMapper.class, CommentMapper.class})
public interface EventMapper {

    NewEvent toModel(NewEventDto newEventDto);

    EventFullDto toDto(Event addedEvent);

    List<EventFullDto> toDtoList(List<Event> events);

    EventShortDto toShortDto(Event event);

    List<EventShortDto> toShortDtoList(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEvent(EventUpdateRequest updateEvent, @MappingTarget Event event);

    default Event toFullEvent(NewEvent newEventDto, Category category, User initiator, EventState state, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .title(newEventDto.getTitle())
                .eventDate(newEventDto.getEventDate())
                .category(category)
                .state(state)
                .initiator(initiator)
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .location(location)
                .build();
    }
}
