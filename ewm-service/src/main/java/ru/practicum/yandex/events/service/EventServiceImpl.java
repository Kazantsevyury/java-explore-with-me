package ru.practicum.yandex.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventSort;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.events.repository.EventSpecification;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.yandex.events.repository.EventSpecification.eventDateInRange;
import static ru.practicum.yandex.events.repository.EventSpecification.eventStatusEquals;
import static ru.practicum.yandex.events.repository.EventSpecification.isAvailable;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size) {
        Sort sort = getSort(searchFilter.getSort());
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size, sort);
        List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null), pageRequest).getContent();
        log.info("Запрос мероприятий по фильтру '{}'. Количество найденных мероприятий '{}'.", searchFilter, events.size());
        return events;
    }

    @Override
    public Event getFullEventInfoById(Long id, Long views) {
        Event event = getEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Мероприятие с id '" + id + "' не опубликовано. Состояние: '" + event.getState() + "'");
        }
        event.setViews(views);
        eventRepository.save(event);
        log.info("Запрос полной информации о мероприятии с id '{}'.", id);
        return event;
    }

    @Override
    public List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventAdminSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null), pageRequest).getContent();
        log.info("Запрос полной информации о мероприятиях от администратора по фильтру '{}'. Количество '{}'.", searchFilter, events.size());
        return events;
    }

    @Override
    @Transactional
    public Event updateEventByAdmin(Long eventId, EventUpdateRequest updateRequest) {
        Event event = getEvent(eventId);
        eventMapper.updateEvent(updateRequest, event);
        updateEventState(updateRequest.getStateAction(), event);
        Event savedEvent = eventRepository.save(event);
        log.info("Мероприятие с id '{}' обновлено администратором.", eventId);
        return savedEvent;
    }

    private void updateEventState(StateAction stateAction, Event event) {
        if (stateAction == null) {
            return;
        }
        switch (stateAction) {
            case PUBLISH_EVENT:
                checkIfEventIsCanceled(event);
                checkIfEventIsAlreadyPublished(event);
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                checkIfEventIsAlreadyPublished(event);
                event.setState(EventState.CANCELED);
                break;
        }
    }

    private void checkIfEventIsCanceled(Event event) {
        if (event.getState().equals(EventState.CANCELED)) {
            throw new NotAuthorizedException("Опубликовать отменённое мероприятие нельзя.");
        }
    }

    private void checkIfEventIsAlreadyPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Мероприятие уже опубликовано.");
        }
    }

    private Event getEvent(Long id) {
        return eventRepository.findFullEventById(id)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id '" + id + "' не найдено."));
    }

    private Sort getSort(EventSort eventSort) {
        Sort sort = Sort.unsorted();
        if (eventSort == null) {
            return sort;
        }

        switch (eventSort) {
            case VIEWS:
                sort = Sort.by(Sort.Direction.DESC, "views");
                break;
            case EVENT_DATE:
                sort = Sort.by(Sort.Direction.DESC, "eventDate");
                break;
            default:
                throw new IllegalArgumentException("Сортировка '" + eventSort + "' пока не поддерживается.");
        }
        return sort;
    }

    private List<Specification<Event>> eventSearchFilterToSpecifications(EventSearchFilter searchFilter) {
        return Stream.of(
                        Optional.of(eventStatusEquals(EventState.PUBLISHED)),
                        Optional.ofNullable(searchFilter.getText()).map(EventSpecification::textInAnnotationOrDescriptionIgnoreCase),
                        Optional.ofNullable(searchFilter.getCategories()).map(EventSpecification::categoriesIdIn),
                        Optional.ofNullable(searchFilter.getPaid()).map(EventSpecification::isPaid),
                        Optional.ofNullable(searchFilter.getRangeStart()).flatMap(start -> Optional.ofNullable(searchFilter.getRangeEnd())
                                .map(end -> eventDateInRange(start, end))),
                        Optional.of(eventIsAvailable(searchFilter.isOnlyAvailable()))
                ).flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private List<Specification<Event>> eventAdminSearchFilterToSpecifications(EventAdminSearchFilter searchFilter) {
        return Stream.of(
                        Optional.ofNullable(searchFilter.getStates()).map(EventSpecification::eventStatusIn),
                        Optional.ofNullable(searchFilter.getUsers()).map(EventSpecification::initiatorIdIn),
                        Optional.ofNullable(searchFilter.getCategories()).map(EventSpecification::categoriesIdIn),
                        Optional.ofNullable(searchFilter.getRangeStart()).flatMap(start -> Optional.ofNullable(searchFilter.getRangeEnd())
                                .map(end -> eventDateInRange(start, end))),
                        Optional.of(eventIsAvailable(searchFilter.isOnlyAvailable()))
                ).flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Specification<Event> eventIsAvailable(boolean isAvailable) {
        return isAvailable ? isAvailable(isAvailable) : null;
    }

}
