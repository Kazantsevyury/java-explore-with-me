package ru.practicum.yandex.user.service;

import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateDto;
import ru.practicum.yandex.user.dto.EventRequestStatusUpdateRequest;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.ParticipationRequest;
import ru.practicum.yandex.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(User userToAdd);

    List<User> getUsers(List<Long> ids, Long from, Integer size);

    void deleteUser(Long userId);

    Event addEventByUser(Long userId, NewEvent newEvent);

    List<Event> findEventsFromUser(Long userId, Long from, Integer size);

    Event getFullEventByInitiator(Long userId, Long eventId);

    Event updateEvent(Long userId, Long eventId, EventUpdateRequest updateEvent);

    ParticipationRequest addParticipationRequestToEvent(Long userId, Long eventId);

    List<ParticipationRequest> findParticipationRequestsByUser(Long userId);

    ParticipationRequest cancelOwnParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequest> findParticipationRequestsForUsersEvent(Long userId, Long eventId);

    EventRequestStatusUpdateDto changeParticipationRequestStatusForUsersEvent(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdate);
}
