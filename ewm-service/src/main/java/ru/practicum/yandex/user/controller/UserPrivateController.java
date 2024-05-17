package ru.practicum.yandex.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.events.dto.EventFullDto;
import ru.practicum.yandex.events.dto.EventShortDto;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.mapper.EventMapper;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.ParticipationRequestDto;
import ru.practicum.yandex.user.mapper.ParticipationMapper;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Private (for registered users) API for events and participation requests.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserPrivateController {

    private final UserService userService;

    private final EventMapper eventMapper;

    private final ParticipationMapper participationMapper;

    /**
     * Add new event. Event date must be at least 2 hours after current time. If event added successfully, returns 201
     * response status.
     *
     * @param userId      event initiator id
     * @param newEventDto event parameters
     * @return added event
     */
    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("User with id '{}' adding new event '{}'.", userId, newEventDto.getTitle());
        final NewEvent newEvent = eventMapper.toModel(newEventDto);
        final Event addedEvent = userService.addEventByUser(userId, newEvent);
        return eventMapper.toDto(addedEvent);
    }

    /**
     * Find events added by user. If nothing found according to search filter, returns empty list.
     *
     * @param userId requester id
     * @param from   first element to display
     * @param size   number of elements to display
     * @return list of events
     */
    @GetMapping("/{userId}/events")
    public List<EventShortDto> findEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Finding events from user with id '{}'.", userId);
        final List<Event> events = userService.findEventsFromUser(userId, from, size);
        return eventMapper.toShortDtoList(events);
    }

    /**
     * Get full event info requested by event initiator. If nothing was found, returns 404 response status.
     *
     * @param userId  requester id
     * @param eventId event id to find
     * @return found event
     */
    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getFullEventByInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Requesting full event with id '{}' by user with id '{}.", eventId, userId);
        final Event event = userService.getFullEventByInitiator(userId, eventId);
        return eventMapper.toDto(event);
    }

    /**
     * Update event information. If event is published, it can not be modified (otherwise returns 409 response status).
     * Event date must be at least 2 hours after current time.
     *
     * @param userId      requester id
     * @param eventId     event id to be modified
     * @param updateEvent event parameters to update
     * @return updated event
     */
    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid EventUpdateRequest updateEvent) {
        log.info("Updating event with id '{}', by user with id '{}'.", eventId, userId);
        final Event updatedEvent = userService.updateEvent(userId, eventId, updateEvent);
        return eventMapper.toDto(updatedEvent);
    }

    /**
     * Find information about participation requests in event by event initiator. If nothing found, returns empty list.
     *
     * @param userId  requester id
     * @param eventId event id
     * @return participation requests in event
     */
    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsForUsersEvent(@PathVariable Long userId,
                                                                                @PathVariable Long eventId) {
        log.info("Getting participation requests in event with id '{}' initiated by user with id '{}'.",
                eventId, userId);
        final List<ParticipationRequest> participationRequests = userService
                .findParticipationRequestsForUsersEvent(userId, eventId);
        return participationMapper.toDtoList(participationRequests);
    }

    /**
     * Modify participation request status for an event. If participation limit is reached, returns 409 response status.
     * If participation requests status is anything but PENDING, returns 409 response status.
     *
     * @param userId       requester id
     * @param eventId      event id
     * @param statusUpdate request parameters to update
     * @return result of participation requests status change
     */
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateDto changeParticipationRequestStatusForUsersEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdate) {
        log.info("Changing participation requests status for event with id '{}' by user with id '{}'.", eventId, userId);
        return userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, statusUpdate);
    }

    /**
     * Add participation request in event. If participation request saved successfully, returns 201 response status.
     *
     * @param userId  requester id
     * @param eventId event id to participate in
     * @return saved participation request
     */
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestToEvent(@PathVariable Long userId,
                                                                  @RequestParam Long eventId) {
        log.info("User with id '{}' requesting participation in event with id '{}'.", userId, eventId);
        final ParticipationRequest participationRequest = userService.addParticipationRequestToEvent(userId, eventId);
        return participationMapper.toDto(participationRequest);
    }

    /**
     * Find user's participation requests. If nothing found, returns empty list.
     *
     * @param userId user id
     * @return participation requests
     */
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsByUser(@PathVariable Long userId) {
        log.info("User with id '{}' requesting participation request list.", userId);
        final List<ParticipationRequest> participationRequests = userService.findParticipationRequestsByUser(userId);
        return participationMapper.toDtoList(participationRequests);
    }

    /**
     * Cancel user's participation requests. Only author of request can cancel it.
     *
     * @param userId    requester id
     * @param requestId request id to cancel
     * @return canceled request
     */
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelOwnParticipationRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.info("User with id '{}' canceling request with id '{}'.", userId, requestId);
        final ParticipationRequest canceledRequest = userService.cancelOwnParticipationRequest(userId, requestId);
        return participationMapper.toDto(canceledRequest);
    }
}
