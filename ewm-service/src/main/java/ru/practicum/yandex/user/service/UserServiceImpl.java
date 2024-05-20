package ru.practicum.yandex.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
     * Добавление нового пользователя.
     *
     * @param userToAdd параметры нового пользователя
     * @return добавленный пользователь
     */
    @Override
    public User createUser(User userToAdd) {
        final User savedUser = userRepository.save(userToAdd);
        log.info("Пользователь с id '{}' создан.", savedUser.getId());
        return savedUser;
    }

    @Override
    public List<User> getUsers(List<Long> ids, Long from, Integer size) {
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);

        Page<User> usersPage;
        if (ids == null || ids.isEmpty()) {
            usersPage = userRepository.findAll(pageRequest);
        } else {
            usersPage = userRepository.findByIdIn(ids, pageRequest);
        }

        List<User> users = usersPage.getContent();
        log.info("Запрос пользователей с ids = '{}', от = '{}', размер = '{}'.", ids, from, size);
        return users;
    }

    /**
     * Удаление пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя для удаления
     */
    @Override
    public void deleteUser(Long userId) {
        getUser(userId);
        log.info("Удаление пользователя с id '{}'.", userId);
        userRepository.deleteById(userId);
    }

    /**
     * Добавление нового события. Если пользователь не найден, выбрасывает NotFoundException.
     *
     * @param userId   идентификатор инициатора события
     * @param newEvent параметры события
     * @return добавленное событие
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
        log.info("Событие с id '{}' было сохранено.", savedEvent.getId());
        return savedEvent;
    }

    /**
     * Поиск событий, добавленных пользователем. Если по фильтру поиска ничего не найдено, возвращает пустой список.
     *
     * @param userId идентификатор запрашивающего пользователя
     * @param from   первый элемент для отображения
     * @param size   количество элементов для отображения
     * @return список событий
     */
    @Override
    public List<Event> findEventsFromUser(Long userId, Long from, Integer size) {
        getUser(userId);
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        final List<Event> userEvents = eventRepository.findEventsByUserId(userId, pageRequest);
        log.info("Запрос событий от пользователя с id '{}'. Найдено событий: '{}'.", userId, userEvents.size());
        return userEvents;
    }

    /**
     * Получение полной информации о событии, запрошенной инициатором. Если пользователь или событие не найдено, выбрасывает NotFoundException.
     * Если пользователь не является инициатором события, выбрасывает NotAuthorizedException.
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события для поиска
     * @return найденное событие
     */
    @Override
    public Event getFullEventByInitiator(Long userId, Long eventId) {
        getUser(userId);
        final Event foundEvent = getEvent(eventId);
        checkIfUserIsEventInitiator(userId, foundEvent);
        log.info("Запрос информации о событии с id '{}' пользователем с id '{}'.", eventId, userId);
        return foundEvent;
    }

    /**
     * Обновление информации о событии. Изменять можно только неопубликованные события (иначе выбрасывает EventNotModifiableException).
     * Если пользователь или событие не найдено, выбрасывает NotFoundException.
     *
     * @param userId      идентификатор запрашивающего пользователя
     * @param eventId     идентификатор события для изменения
     * @param updateEvent параметры для обновления события
     * @return обновленное событие
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
        log.info("Событие с id '{}' было обновлено пользователем с id '{}'.", eventId, userId);
        return updatedEvent;
    }

    /**
     * Поиск информации о запросах на участие в событии инициатором события. Если пользователь или событие не найдено,
     * выбрасывает NotFoundException. Если пользователь не является инициатором события, выбрасывает NotAuthorizedException.
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события
     * @return запросы на участие в событии
     */
    @Override
    public List<ParticipationRequest> findParticipationRequestsForUsersEvent(Long userId, Long eventId) {
        getUser(userId);
        final Event event = getEvent(eventId);
        checkIfUserIsEventInitiator(userId, event);
        final List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventId(eventId);
        log.info("Получение запросов на участие для события с id '{}' пользователем с id '{}'.", eventId, userId);
        return participationRequests;
    }

    /**
     * Изменение статуса запроса на участие в событии. Изменять можно только неопубликованные события. Если лимит
     * участников события равен нулю или предварительная модерация отключена, все запросы автоматически подтверждаются.
     * Если пользователь или событие не найдено, выбрасывает NotFoundException. Если лимит участников достигнут, все
     * оставшиеся запросы на участие будут автоматически отклонены.
     *
     * @param userId       идентификатор запрашивающего пользователя
     * @param eventId      идентификатор события
     * @param statusUpdate параметры для обновления статуса запроса
     * @return результат изменения статуса запроса на участие
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
        log.info("Статус участия для события с id '{}' был обновлен пользователем с id '{}'. Запрос на обновление: '{}'.",
                eventId, userId, statusUpdate);
        return eventRequestStatusUpdate;
    }

    /**
     * Добавление запроса на участие в событии. Только один запрос на участие может быть добавлен пользователем к одному событию.
     * Инициатор события не может добавить запрос на участие в свое собственное событие. Запрос на участие может быть добавлен
     * только к опубликованному событию. Если лимит участников равен нулю или предварительная модерация события отключена,
     * запросы автоматически подтверждаются. Запросы могут быть подтверждены до тех пор, пока лимит участников не превышен
     * (если не установлен в ноль).
     *
     * @param userId  идентификатор запрашивающего пользователя
     * @param eventId идентификатор события для участия
     * @return сохраненный запрос на участие
     */
    @Override
    @Transactional
    public ParticipationRequest addParticipationRequestToEvent(Long userId, Long eventId) {
        final User user = getUser(userId);
        final Event event = getEvent(eventId);
        checkIfUserCanMakeRequest(userId, eventId, event);
        checkIfParticipationRequestExists(userId, eventId);
        checkIfEventIsPublished(event, userId);
        log.info("Пользователь с id '{}' добавил запрос на участие для события с id '{}'.", userId, eventId);
        final ParticipationRequest participationRequest = createParticipantRequest(user, event);
        final ParticipationRequest savedRequest = participationRequestRepository.save(participationRequest);
        log.info("Запрос на участие с id '{}' был сохранен. Текущее количество участников события с id '{}' составляет '{}'.",
                participationRequest.getId(), eventId, event.getNumberOfParticipants());
        return savedRequest;
    }

    /**
     * Поиск запросов на участие пользователя.
     *
     * @param userId идентификатор пользователя
     * @return запросы на участие
     */
    @Override
    public List<ParticipationRequest> findParticipationRequestsByUser(Long userId) {
        getUser(userId);
        final List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByRequesterId(userId);
        log.info("Пользователь с id '{}' запрашивает список запросов на участие размером '{}'.", userId, participationRequests.size());
        return participationRequests;
    }

    /**
     * Отмена запросов на участие пользователя. Только автор запроса может его отменить.
     *
     * @param userId    идентификатор запрашивающего пользователя
     * @param requestId идентификатор запроса для отмены
     * @return отмененный запрос
     */
    @Override
    @Transactional
    public ParticipationRequest cancelOwnParticipationRequest(Long userId, Long requestId) {
        getUser(userId);
        final ParticipationRequest participationRequest = getParticipationRequest(requestId);
        checkIfUserCanCancelParticipationRequest(userId, participationRequest);
        participationRequest.setStatus(CANCELED);
        log.info("Запрос на участие с id '{}' был отменен пользователем с id '{}'.", participationRequest.getId(), userId);
        return participationRequest;
    }

    private int populateStatusUpdateDto(EventRequestStatusUpdateRequest statusUpdate, List<ParticipationRequest> participationRequests, EventRequestStatusUpdateDto eventRequestStatusUpdate, int lastConfirmedRequest, Event event, int participantLimit) {
        for (ParticipationRequest participationRequest : participationRequests) {
            if (!participationRequest.getStatus().equals(PENDING)) {
                throw new NotAuthorizedException("Для изменения статуса запрос должен иметь статус PENDING. Текущий статус: '"
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
            throw new EventNotModifiableException("Событие с id '" + event.getId() + "' не имеет лимита участников или " +
                    "предварительная модерация отключена. Нет необходимости подтверждать запросы. Лимит участников: '" + event.getParticipantLimit()
                    + "', Модерация: '" + event.isRequestModeration() + "'");
        }

        int currentParticipants = event.getNumberOfParticipants();

        if (currentParticipants == participantLimit) {
            throw new NotAuthorizedException("Лимит участников достигнут");
        }
        return participantLimit;
    }

    private void checkIfUserIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotAuthorizedException("Пользователь с id '" + userId + "' не является инициатором события с id '" +
                    event.getId() + "'.");
        }
    }

    private void checkIfUserCanCancelParticipationRequest(Long userId, ParticipationRequest participationRequest) {
        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new NotAuthorizedException("Пользователь с id '" + userId + "' не уполномочен отменять запрос на участие с" +
                    " id '" + participationRequest.getId() + "'.");
        }
    }

    private ParticipationRequest getParticipationRequest(Long requestId) {
        return participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие с id '" + requestId + "' не найден."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }

    private Event createNewEvent(NewEvent newEvent, Category category, User initiator, Location eventLocation) {
        return eventMapper.toFullEvent(newEvent, category, initiator, EventState.PENDING, eventLocation);
    }

    private Location saveLocation(NewEvent newEvent) {
        final Location eventLocation = locationRepository.save(newEvent.getLocation());
        log.info("Служба пользователей, местоположение '{}' было сохранено.", eventLocation);
        return eventLocation;
    }

    private Category getCategory(NewEvent newEvent) {
        return categoryRepository.findById(newEvent.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория с id '" + newEvent.getCategoryId() + "' не найдена."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findFullEventById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id '" + eventId + "' не найдено."));
    }

    private void checkEventIsPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotModifiableException("Опубликованное событие с id '" + event.getId() + "' не может быть изменено.");
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
            throw new RequestAlreadyExistsException("Запрос на участие от пользователя с id '" + userId + "' для события " +
                    "с id '" + eventId + "' уже существует.");
        }
    }

    private void checkIfUserCanMakeRequest(Long userId, Long eventId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new NotAuthorizedException("Инициатор с id '" + userId + "' не может сделать запрос на участие " +
                    "в своем собственном событии с id '" + eventId + "'.");
        }
    }

    private void checkIfEventIsPublished(Event event, Long userId) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Пользователь с id '" + userId + "' не может сделать запрос на участие в неопубликованном событии " +
                    "с id '" + event.getId() + "'.");
        }
    }

    private ParticipationRequest createParticipantRequest(User user, Event event) {
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .build();
        if (event.getNumberOfParticipants() == event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new NotAuthorizedException("Лимит участников превышен для события с id '" + event.getId() + "'.");
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
