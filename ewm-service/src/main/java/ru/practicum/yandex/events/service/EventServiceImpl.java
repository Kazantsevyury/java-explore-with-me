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
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.yandex.events.repository.EventSpecification.categoriesIdIn;
import static ru.practicum.yandex.events.repository.EventSpecification.eventDateInRange;
import static ru.practicum.yandex.events.repository.EventSpecification.eventStatusEquals;
import static ru.practicum.yandex.events.repository.EventSpecification.eventStatusIn;
import static ru.practicum.yandex.events.repository.EventSpecification.initiatorIdIn;
import static ru.practicum.yandex.events.repository.EventSpecification.isAvailable;
import static ru.practicum.yandex.events.repository.EventSpecification.isPaid;
import static ru.practicum.yandex.events.repository.EventSpecification.textInAnnotationOrDescriptionIgnoreCase;

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
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting events with filter '{}'. List size '{}.", searchFilter, events.size());
        return events;
    }

    @Override
    public Event getFullEventInfoById(Long id, Long views) {
        Event event = getEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id '" + id + "' is not published. State: '" + event.getState() + "'");
        }
        event.setViews(views);
        eventRepository.save(event);
        log.info("Requesting full event info with id '{}'.", id);
        return event;
    }

    @Override
    public List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventAdminSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting full events info by admin  with filter '{}'. List size '{}'.", searchFilter, events.size());
        return events;
    }

    @Override
    @Transactional
    public Event updateEventByAdmin(Long eventId, EventUpdateRequest updateRequest) {
        Event event = getEvent(eventId);
        eventMapper.updateEvent(updateRequest, event);
        updateEventState(updateRequest.getStateAction(), event);
        Event savedEvent = eventRepository.save(event);
        log.info("Event with id '{}' was updated by admin.", eventId);
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
            throw new NotAuthorizedException("Can not publish canceled event.");
        }
    }

    private void checkIfEventIsAlreadyPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Event is already published.");
        }
    }

    private Event getEvent(Long id) {
        return eventRepository.findFullEventById(id)
                .orElseThrow(() -> new NotFoundException("Event with id '" + id + "' was not found."));
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
                throw new IllegalArgumentException("Sort '" + eventSort + "is not supported yet.");
        }
        return sort;
    }

    private List<Specification<Event>> eventSearchFilterToSpecifications(EventSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(eventStatusEquals(EventState.PUBLISHED));
        resultSpecification.add(textInAnnotationOrDescriptionIgnoreCase(searchFilter.getText()));
        resultSpecification.add(categoriesIdIn(searchFilter.getCategories()));
        resultSpecification.add(isPaid(searchFilter.getPaid()));
        resultSpecification.add(eventDateInRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        resultSpecification.add(isAvailable(searchFilter.isOnlyAvailable()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Specification<Event>> eventAdminSearchFilterToSpecifications(EventAdminSearchFilter searchFilter) {
        List<Specification<Event>> resultSpecification = new ArrayList<>();
        resultSpecification.add(eventStatusIn(searchFilter.getStates()));
        resultSpecification.add(initiatorIdIn(searchFilter.getUsers()));
        resultSpecification.add(categoriesIdIn(searchFilter.getCategories()));
        resultSpecification.add(eventDateInRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        resultSpecification.add(isAvailable(searchFilter.isOnlyAvailable()));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
