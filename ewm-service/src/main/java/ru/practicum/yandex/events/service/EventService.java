package ru.practicum.yandex.events.service;

import ru.practicum.yandex.events.dto.EventAdminSearchFilter;
import ru.practicum.yandex.events.dto.EventSearchFilter;
import ru.practicum.yandex.events.dto.EventUpdateRequest;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.Event;

import java.util.List;

public interface EventService {
    List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size);

    Event getFullEventInfoById(Long id, Long views);

    List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size);

    Event updateEventByAdmin(Long eventId, EventUpdateRequest updateRequest);

    Event addCommentToEvent(Long userId, Long eventId, Comment commentRequest);

    Event updateComment(Long userId, Long commentId, Comment commentRequest);

    void deleteComment(Long userId, Long commentId);
}
