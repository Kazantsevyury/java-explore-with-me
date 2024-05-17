package ru.practicum.yandex.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventSort;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.repository.CommentRepository;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.events.repository.EventSpecification;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.repository.UserRepository;

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

    private final UserRepository userRepository;

    private final CommentRepository commentRepository;

    private final EventMapper eventMapper;

    /**
     * Find event according to search filter. Only published events will be displayed. Text search (in annotation and
     * description) is ignore case. If no date range is specified, than event with event date after current date will be
     * displayed.
     *
     * @param searchFilter search filter
     * @param from         first element to display
     * @param size         number of elements to display
     * @return list of events
     */
    @Override
    public List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        Specification<Event> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        List<Event> events = eventRepository.findAll(getSort(searchFilter.getSort(), resultSpec),
                pageRequest).getContent();
        log.info("Requesting events with filter '{}'. List size '{}.", searchFilter, events.size());
        return events;
    }

    /**
     * Get full event info by event id. Event must be published.
     *
     * @param id    event id to find
     * @param views number of event views
     * @return found event
     */
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

    /**
     * Find full events info according to search filter. If nothing was found, returns empty list.
     *
     * @param searchFilter search filter
     * @param from         first element to display
     * @param size         number of elements to display
     * @return found events
     */
    @Override
    public List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventAdminSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Requesting full events info by admin  with filter '{}'. List size '{}'.", searchFilter, events.size());
        return events;
    }

    /**
     * Modify event parameters and status. Event must have state 'PENDING' to be modified. Only not published event can
     * be canceled.
     *
     * @param eventId       event id to modify
     * @param updateRequest event parameters to update
     * @return updated event
     */
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

    /**
     * Add comment to event.
     *
     * @param userId  user id adding comment
     * @param eventId event id to comment
     * @param comment comment
     * @return added comment
     */
    @Override
    public Event addCommentToEvent(Long userId, Long eventId, Comment comment) {
        final User user = getUser(userId);
        final Event event = getEvent(eventId);
        comment.setAuthor(user);
        comment.setEvent(event);
        Comment savedComment = commentRepository.save(comment);
        event.addCommentToEvent(savedComment);
        eventRepository.save(event);
        log.info("User with id '{}' added comment to event with id '{}'.", userId, eventId);
        return event;
    }

    /**
     * Update comment. Only author of comment can update comment.
     *
     * @param userId        user updating comment
     * @param eventId       event comment to update
     * @param updateComment update comment
     * @return updated comment
     */
    @Override
    public Event updateComment(Long userId, Long eventId, Comment updateComment) {
        getUser(userId);
        Comment comment = getComment(updateComment.getId());
        checkIfUserIsCommentAuthor(userId, comment);
        comment.setText(updateComment.getText());
        Comment updatedComment = commentRepository.save(comment);
        Event event = getEvent(eventId);
        log.info("Comment with id '" + updatedComment.getId() + "' was updated.");
        return event;
    }

    /**
     * Delete comment. Only author of comment can delete comment.
     *
     * @param userId    user deleting comment
     * @param commentId comment id to delete
     */
    @Override
    public void deleteComment(Long userId, Long commentId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        checkIfUserIsCommentAuthor(userId, comment);
        commentRepository.deleteById(commentId);
        log.info("Comment with id '" + commentId + "' was deleted by user with id '" + userId + "'.");
    }

    private void checkIfUserIsCommentAuthor(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NotAuthorizedException("User with id '" + userId + "' is not author of comment with id '" +
                    comment.getId() + "'.");
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id '" + commentId + "' not found."));
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

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found."));
    }

    private Specification<Event> getSort(EventSort eventSort, Specification<Event> spec) {
        if (eventSort == null) {
            return EventSpecification.orderById(spec);
        }
        switch (eventSort) {
            case VIEWS:
                return EventSpecification.orderByViews(spec);
            case EVENT_DATE:
                return EventSpecification.orderByEventDate(spec);
            case MOST_COMMENTS:
                return EventSpecification.orderByNumberOfComments(spec);
            default:
                throw new IllegalArgumentException("Sort '" + eventSort + "is not supported yet.");
        }
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
