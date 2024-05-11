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

import static ru.practicum.yandex.events.repository.EventSpecification.*;

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
        List<Specification<Event>> specs = new ArrayList<>();
        specs.add(eventStatusEquals(EventState.PUBLISHED));
        if (searchFilter.getText() != null) {
            specs.add(textInAnnotationOrDescriptionIgnoreCase(searchFilter.getText()));
        }
        if (searchFilter.getCategories() != null) {
            specs.add(categoriesIdIn(searchFilter.getCategories()));
        }
        if (searchFilter.getPaid() != null) {
            specs.add(isPaid(searchFilter.getPaid()));
        }
        if (searchFilter.getRangeStart() != null && searchFilter.getRangeEnd() != null) {
            specs.add(eventDateInRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        }
        if (searchFilter.isOnlyAvailable() != null) {
            specs.add(isAvailable(searchFilter.isOnlyAvailable()));
        }
        return specs;
    }

    private List<Specification<Event>> eventAdminSearchFilterToSpecifications(EventAdminSearchFilter searchFilter) {
        List<Specification<Event>> specs = new ArrayList<>();
        if (searchFilter.getStates() != null) {
            specs.add(eventStatusIn(searchFilter.getStates()));
        }
        if (searchFilter.getUsers() != null) {
            specs.add(initiatorIdIn(searchFilter.getUsers()));
        }
        if (searchFilter.getCategories() != null) {
            specs.add(categoriesIdIn(searchFilter.getCategories()));
        }
        if (searchFilter.getRangeStart() != null && searchFilter.getRangeEnd() != null) {
            specs.add(eventDateInRange(searchFilter.getRangeStart(), searchFilter.getRangeEnd()));
        }
        if (searchFilter.isOnlyAvailable() != null) {
            specs.add(isAvailable(searchFilter.isOnlyAvailable()));
        }
        return specs;
    }

}
