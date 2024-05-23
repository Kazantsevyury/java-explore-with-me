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
 * Приватный (для зарегистрированных пользователей) API для событий и запросов на участие.
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
     * Добавить новое событие. Дата события должна быть не менее чем через 2 часа от текущего времени.
     * Если событие добавлено успешно, возвращает статус 201.
     *
     * @param userId      идентификатор инициатора события
     * @param newEventDto параметры события
     * @return добавленное событие
     */
    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Пользователь с id '{}' добавляет новое событие '{}'.", userId, newEventDto.getTitle());
        final NewEvent newEvent = eventMapper.toModel(newEventDto);
        final Event addedEvent = userService.addEventByUser(userId, newEvent);
        return eventMapper.toDto(addedEvent);
    }

    /**
     * Найти события, добавленные пользователем. Если по фильтру поиска ничего не найдено, возвращает пустой список.
     *
     * @param userId идентификатор запрашивающего пользователя
     * @param from   первый элемент для отображения
     * @param size   количество элементов для отображения
     * @return список событий
     */
    @GetMapping("/{userId}/events")
    public List<EventShortDto> findEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Поиск событий от пользователя с id '{}'.", userId);
        final List<Event> events = userService.findEventsFromUser(userId, from, size);
        return eventMapper.toShortDtoList(events);
    }

    /**
     * Получить полную информацию о событии, запрошенном инициатором. Если ничего не найдено, возвращает статус 404.
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события
     * @return найденное событие
     */
    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getFullEventByInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос полной информации о событии с id '{}' пользователем с id '{}'.", eventId, userId);
        final Event event = userService.getFullEventByInitiator(userId, eventId);
        return eventMapper.toDto(event);
    }

    /**
     * Обновить информацию о событии. Если событие опубликовано, его нельзя изменить (иначе возвращает статус 409).
     * Дата события должна быть не менее чем через 2 часа от текущего времени.
     *
     * @param userId      идентификатор запрашивающего пользователя
     * @param eventId     идентификатор события для изменения
     * @param updateEvent параметры для обновления события
     * @return обновленное событие
     */
    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid EventUpdateRequest updateEvent) {
        log.info("Обновление события с id '{}', пользователем с id '{}'.", eventId, userId);
        final Event updatedEvent = userService.updateEvent(userId, eventId, updateEvent);
        return eventMapper.toDto(updatedEvent);
    }

    /**
     * Найти информацию о запросах на участие в событии инициатором события. Если ничего не найдено, возвращает пустой список.
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события
     * @return запросы на участие в событии
     */
    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsForUsersEvent(@PathVariable Long userId,
                                                                                @PathVariable Long eventId) {
        log.info("Получение запросов на участие в событии с id '{}', инициированном пользователем с id '{}'.",
                eventId, userId);
        final List<ParticipationRequest> participationRequests = userService
                .findParticipationRequestsForUsersEvent(userId, eventId);
        return participationMapper.toDtoList(participationRequests);
    }

    /**
     * Изменить статус запроса на участие в событии. Если лимит участников достигнут, возвращает статус 409.
     * Если статус запроса на участие не PENDING, возвращает статус 409.
     *
     * @param userId       идентификатор запрашивающего пользователя
     * @param eventId      идентификатор события
     * @param statusUpdate параметры для обновления статуса запроса
     * @return результат изменения статуса запроса на участие
     */
    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateDto changeParticipationRequestStatusForUsersEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdate) {
        log.info("Изменение статуса запроса на участие в событии с id '{}' пользователем с id '{}'.", eventId, userId);
        return userService.changeParticipationRequestStatusForUsersEvent(userId, eventId, statusUpdate);
    }

    /**
     * Добавить запрос на участие в событии. Если запрос на участие сохранен успешно, возвращает статус 201.
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события для участия
     * @return сохраненный запрос на участие
     */
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestToEvent(@PathVariable Long userId,
                                                                  @RequestParam Long eventId) {
        log.info("Пользователь с id '{}' запрашивает участие в событии с id '{}'.", userId, eventId);
        final ParticipationRequest participationRequest = userService.addParticipationRequestToEvent(userId, eventId);
        return participationMapper.toDto(participationRequest);
    }

    /**
     * Найти запросы на участие пользователя. Если ничего не найдено, возвращает пустой список.
     *
     * @param userId идентификатор пользователя
     * @return запросы на участие
     */
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findParticipationRequestsByUser(@PathVariable Long userId) {
        log.info("Пользователь с id '{}' запрашивает список запросов на участие.", userId);
        final List<ParticipationRequest> participationRequests = userService.findParticipationRequestsByUser(userId);
        return participationMapper.toDtoList(participationRequests);
    }

    /**
     * Отменить запрос на участие пользователя. Только автор запроса может его отменить.
     *
     * @param userId    идентификатор запрашивающего пользователя
     * @param requestId идентификатор запроса для отмены
     * @return отмененный запрос
     */
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelOwnParticipationRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.info("Пользователь с id '{}' отменяет запрос с id '{}'.", userId, requestId);
        final ParticipationRequest canceledRequest = userService.cancelOwnParticipationRequest(userId, requestId);
        return participationMapper.toDto(canceledRequest);
    }
}
