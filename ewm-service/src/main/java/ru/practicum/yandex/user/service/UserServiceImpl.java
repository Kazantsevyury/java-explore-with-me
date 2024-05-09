package ru.practicum.yandex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.category.repository.CategoryRepository;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;
import ru.practicum.yandex.events.model.Location;
import ru.practicum.yandex.events.repository.CommentRepository;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.events.repository.LocationRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.EventNotModifiableException;
import ru.practicum.yandex.shared.exception.NotAuthorizedException;
import ru.practicum.yandex.shared.exception.NotFoundException;
import ru.practicum.yandex.shared.exception.RequestAlreadyExistsException;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.User;
import ru.practicum.yandex.user.repository.ParticipationRequestRepository;
import ru.practicum.yandex.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static ru.practicum.yandex.user.model.ParticipationStatus.CANCELED;
import static ru.practicum.yandex.user.model.ParticipationStatus.CONFIRMED;
import static ru.practicum.yandex.user.model.ParticipationStatus.PENDING;
import static ru.practicum.yandex.user.model.ParticipationStatus.REJECTED;
import static ru.practicum.yandex.user.repository.UserSpecification.idIn;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private final LocationRepository locationRepository;

    private final ParticipationRequestRepository participationRequestRepository;

    private final CommentRepository commentRepository;

    private final EventMapper eventMapper;

    private final ParticipationMapper participationMapper;

    /**
     * Add new user.
     *
     * @param userToAdd new user parameters
     * @return added user
     */
    @Override
    public User createUser(User userToAdd) {
        final User savedUser = userRepository.save(userToAdd);
        log.info("User with id '{}' created.", savedUser.getId());
        return savedUser;
    }

    /**
     * Get information about users. If nothing found, returns empty list.
     *
     * @param ids  users ids to search in
     * @param from first element to display
     * @param size number of elements to display
     * @return found users
     */
    @Override
    public List<User> getUsers(List<Long> ids, Long from, Integer size) {
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Specification<User> idIn = idIn(ids);
        List<User> users = userRepository.findAll(idIn, pageRequest).getContent();
        log.info("Requesting users with ids = '{}', from = '{}', size = '{}'.", ids, from, size);
        return users;
    }

    /**
     * Delete user by user id.
     *
     * @param userId user id to delete
     */
    @Override
    public void deleteUser(Long userId) {
        getUser(userId);
        log.info("Deleting user with id '" + userId + "'.");
        userRepository.deleteById(userId);
    }

    /**
     * Add new event. If user was not found, throws NotFoundException.
     *
     * @param userId   event initiator id
     * @param newEvent event parameters
     * @return added event
     */
    @Override
    @Transactional
    public Event addEventByUser(Long userId, NewEvent newEvent) {
        final User initiator = getUser(userId);
        final Category category = getCategory(newEvent);
        final Location eventLocation = saveLocation(newEvent);
        Event fullEvent;
        fullEvent = createNewEvent(newEvent, category, initiator, eventLocation);
        final Event savedEvent = eventRepository.save(fullEvent);
        log.info("Event with id '{}' was saved.", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Find events added by user. If nothing found according to search filter, returns empty list.
     *
     * @param userId requester id
     * @param from   first element to display
     * @param size   number of elements to display
     * @return list of events
     */
    @Override
    public List<Event> findEventsFromUser(Long userId, Long from, Integer size) {
        getUser(userId);
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        final List<Event> userEvents = eventRepository.findEventsByUserId(userId, pageRequest);
        log.info("Requesting event from user with id '{}'. Events found: '{}'.", userId, userEvents.size());
        return userEvents;
    }

    /**
     * Get full event info requested by event initiator. If user or event was not found, throws NotFoundException.
     * If user is not event's initiator, throws NotAuthorizedException.
     *
     * @param userId  requester id
     * @param eventId event id to find
     * @return found event
     */
    @Override
    public Event getFullEventByInitiator(Long userId, Long eventId) {
        getUser(userId);
        final Event foundEvent = getEvent(eventId);
        checkIfUserIsEventInitiator(userId, foundEvent);
        log.info("Requesting info about event with id '{}' by user with id '{}'.", eventId, userId);
        return foundEvent;
    }

    /**
     * Update event information. Only not published events can be modified (otherwise throws EventNotModifiableException).
     * If user or event was not found, throws NotFoundException.
     *
     * @param userId      requester id
     * @param eventId     event id to be modified
     * @param updateEvent event parameters to update
     * @return updated event
     */
    @Override
    @Transactional
    public Event updateEvent(Long userId, Long eventId, EventUpdateRequest updateEvent) {
        getUser(userId);
        final Event eventToUpdate = getEvent(eventId);
        checkEventIsPublished(eventToUpdate);
        changeStateIfNeeded(updateEvent, eventToUpdate);
        eventMapper.updateEvent(updateEvent, eventToUpdate);
        Event updatedEvent = eventRepository.save(eventToUpdate);
        log.info("Event with id '{}' was updated by user with id '{}'.", eventId, userId);
        return updatedEvent;
    }

    /**
     * Find information about participation requests in event by event initiator. If user or event was not found,
     * throws NotFoundException. If user is not event's initiator, throws NotAuthorizedException.
     *
     * @param userId  requester id
     * @param eventId event id
     * @return participation requests in event
     */
    @Override
    public List<ParticipationRequest> findParticipationRequestsForUsersEvent(Long userId, Long eventId) {
        getUser(userId);
        final Event event = getEvent(eventId);
        checkIfUserIsEventInitiator(userId, event);
        final List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventId(eventId);
        log.info("Getting participation requests for event with id '{}' by user with id '{}'.", eventId, userId);
        return participationRequests;
    }

    /**
     * Modify participation request status for an event. Only not published events can be modified. If event's
     * participation limit is zero of pre-moderation is off, all requests are confirmed automatically. If user or event
     * was not found, throws NotFoundException. If participation limit is reached, participation all remaining
     * participation requests will be rejected automatically.
     *
     * @param userId       requester id
     * @param eventId      event id
     * @param statusUpdate request parameters to update
     * @return result of participation requests status change
     */
    @Override
    @Transactional
    public EventRequestStatusUpdateDto changeParticipationRequestStatusForUsersEvent(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest statusUpdate) {
        getUser(userId);
        final Event event = getEvent(eventId);
        int participantLimit = checkParticipantLimit(event);
        final List<Long> requestIds = statusUpdate.getRequestIds();
        final List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByIdIn(requestIds);
        int lastConfirmedRequest = 0;
        final EventRequestStatusUpdateDto eventRequestStatusUpdate = new EventRequestStatusUpdateDto();
        lastConfirmedRequest = populateStatusUpdateDto(statusUpdate, participationRequests, eventRequestStatusUpdate, lastConfirmedRequest, event, participantLimit);
        rejectRemainingRequestsAfterExceedingParticipantLimit(lastConfirmedRequest, participationRequests, eventRequestStatusUpdate);
        log.info("Participation status for event with id '{}' was updated by user with id '{}'. Update request: '{}'.",
                eventId, userId, statusUpdate);
        return eventRequestStatusUpdate;
    }

    /**
     * Add participation request in event. Only one participation request can be added by user to the same event. Event
     * initiator can not add participation request to his own event. Participation request can be added only to published
     * event. If participation limit is set to zero or pre-moderation of event is off, requests are confirmed automatically.
     * Requests can be confirmed until participation limit is not exceeded (if not set to zero).
     *
     * @param userId  requester id
     * @param eventId event id to participate in
     * @return saved participation request
     */
    @Override
    @Transactional
    public ParticipationRequest addParticipationRequestToEvent(Long userId, Long eventId) {
        final User user = getUser(userId);
        final Event event = getEvent(eventId);
        checkIfUserCanMakeRequest(userId, eventId, event);
        checkIfParticipationRequestExists(userId, eventId);
        checkIfEventIsPublished(event, userId);
        log.info("User with id '{}' added participation request for event with id '{}'.", userId, eventId);
        final ParticipationRequest participationRequest = createParticipantRequest(user, event);
        final ParticipationRequest savedRequest = participationRequestRepository.save(participationRequest);
        log.info("Participation request with '{}' was saved. Current number of participants on event with id '{}' is '{}'.", participationRequest.getId(),
                eventId, event.getNumberOfParticipants());
        return savedRequest;
    }

    /**
     * Find user's participation requests.
     *
     * @param userId user id
     * @return participation requests
     */
    @Override
    public List<ParticipationRequest> findParticipationRequestsByUser(Long userId) {
        getUser(userId);
        final List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByRequesterId(userId);
        log.info("User with id '{}' requesting event participation list with size '{}'.", userId, participationRequests.size());
        return participationRequests;
    }

    /**
     * Cancel user's participation requests. Only author of request can cancel it.
     *
     * @param userId    requester id
     * @param requestId request id to cancel
     * @return canceled request
     */
    @Override
    @Transactional
    public ParticipationRequest cancelOwnParticipationRequest(Long userId, Long requestId) {
        getUser(userId);
        final ParticipationRequest participationRequest = getParticipationRequest(requestId);
        checkIfUserCanCancelParticipationRequest(userId, participationRequest);
        participationRequest.setStatus(CANCELED);
        log.info("Participation request with id '{}' was canceled by user with id '{}'.", participationRequest.getId(),
                userId);
        return participationRequest;
    }

    private int populateStatusUpdateDto(EventRequestStatusUpdateRequest statusUpdate, List<ParticipationRequest> participationRequests, EventRequestStatusUpdateDto eventRequestStatusUpdate, int lastConfirmedRequest, Event event, int participantLimit) {
        for (ParticipationRequest participationRequest : participationRequests) {
            if (!participationRequest.getStatus().equals(PENDING)) {
                throw new NotAuthorizedException("For status change request must have status PENDING. Current status: '"
                        + participationRequest.getStatus() + "'");
            }
            participationRequest.setStatus(statusUpdate.getStatus());
            participationRequestRepository.save(participationRequest);
            if (statusUpdate.getStatus().equals(CONFIRMED)) {
                eventRequestStatusUpdate.addConfirmedRequest(participationMapper.toDto(participationRequest));
                lastConfirmedRequest++;
                int incrementedParticipants = event.addParticipant();
                eventRepository.save(event);
                if (incrementedParticipants == participantLimit) {
                    break;
                }
            }
        }
        return lastConfirmedRequest;
    }

    private void rejectRemainingRequestsAfterExceedingParticipantLimit(int lastConfirmedRequest, List<ParticipationRequest> participationRequests, EventRequestStatusUpdateDto eventRequestStatusUpdate) {
        for (int i = lastConfirmedRequest; i < participationRequests.size(); i++) {
            ParticipationRequest participationRequest = participationRequests.get(i);
            participationRequest.setStatus(REJECTED);
            participationRequestRepository.save(participationRequest);
            eventRequestStatusUpdate.addRejectedRequest(participationMapper.toDto(participationRequest));
        }
    }

    private static int checkParticipantLimit(Event event) {
        int participantLimit = event.getParticipantLimit();

        if (participantLimit == 0 || !event.isRequestModeration()) {
            throw new EventNotModifiableException("Event with id '" + event.getId() + "' has no participant limit or " +
                    "pre moderation if off. No need to confirm requests. Participant limit: '" + event.getParticipantLimit()
                    + "', Moderation: '" + event.isRequestModeration() + "'");
        }

        int currentParticipants = event.getNumberOfParticipants();

        if (currentParticipants == participantLimit) {
            throw new NotAuthorizedException("The participant limit has been reached");
        }
        return participantLimit;
    }

    private void checkIfUserIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotAuthorizedException("User with id '" + userId + "' is not an initiator of event with id '" +
                    event.getId() + "'.");
        }
    }

    private void checkIfUserCanCancelParticipationRequest(Long userId, ParticipationRequest participationRequest) {
        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new NotAuthorizedException("User with id '" + userId + "' is not authorized to cancel participation request with" +
                    "id '" + participationRequest.getId() + "'.");
        }
    }

    private ParticipationRequest getParticipationRequest(Long requestId) {
        return participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Participation request with id '" + requestId + "' was not found."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found."));
    }

    private Event createNewEvent(NewEvent newEvent, Category category, User initiator, Location eventLocation) {
        return eventMapper.toFullEvent(newEvent, category, initiator, EventState.PENDING, eventLocation);
    }

    private Location saveLocation(NewEvent newEvent) {
        final Location eventLocation = locationRepository.save(newEvent.getLocation());
        log.info("UserService, location '{}' was saved.", eventLocation);
        return eventLocation;
    }

    private Category getCategory(NewEvent newEvent) {
        return categoryRepository.findById(newEvent.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category with id '" + newEvent.getCategoryId() + "' not found."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findFullEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id '" + eventId + "' not found."));
    }

    private void checkEventIsPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotModifiableException("Published event with id '" + event.getId() + "' can not be modified.");
        }
    }

    private void changeStateIfNeeded(EventUpdateRequest updateEvent, Event eventToUpdate) {
        if (updateEvent.getStateAction() == null) {
            return;
        }
        switch (updateEvent.getStateAction()) {
            case CANCEL_REVIEW:
                eventToUpdate.setState(EventState.CANCELED);
                break;
            case SEND_TO_REVIEW:
                eventToUpdate.setState(EventState.PENDING);
                break;
        }
    }

    private void checkIfParticipationRequestExists(Long userId, Long eventId) {
        Optional<ParticipationRequest> participationRequest = participationRequestRepository
                .findByRequesterIdAndEventId(userId, eventId);
        if (participationRequest.isPresent()) {
            throw new RequestAlreadyExistsException("Participation request by user with id '" + userId + "' to event " +
                    "with id '" + eventId + "' already exists.");
        }
    }

    private void checkIfUserCanMakeRequest(Long userId, Long eventId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new NotAuthorizedException("Initiator with id '" + userId + "' can not make participation request " +
                    "to his own event with id '" + eventId + "'.");
        }
    }

    private void checkIfEventIsPublished(Event event, Long userId) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("User with id '" + userId + "'can not make request to not published event " +
                    "with id '" + event.getId() + "'.");
        }
    }

    private ParticipationRequest createParticipantRequest(User user, Event event) {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .build();
        if (event.getNumberOfParticipants() == event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new NotAuthorizedException("Participant limit is exceeded for event with id '" + event.getId() + "'.");
        } else if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            participationRequest.setStatus(CONFIRMED);
            addConfirmedRequestToEvent(event);
        } else {
            participationRequest.setStatus(PENDING);
        }
        return participationRequest;
    }

    private void addConfirmedRequestToEvent(Event event) {
        event.addParticipant();
        eventRepository.save(event);
    }
}
