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
     * Найти мероприятие в соответствии с фильтром поиска. Будут отображаться только опубликованные мероприятия. Поиск текста (в аннотации и
     * описании) нечувствителен к регистру. Если диапазон дат не указан, то будет отображено мероприятие с датой мероприятия после текущей даты.
     *
     * @param searchFilter фильтр поиска
     * @param from         первый элемент для отображения
     * @param size         количество элементов для отображения
     * @return список мероприятий
     */
    @Override
    public List<Event> findEvents(EventSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventSearchFilterToSpecifications(searchFilter);
        Specification<Event> resultSpec = specifications.stream().reduce(Specification::and).orElse(null);
        List<Event> events = eventRepository.findAll(getSort(searchFilter.getSort(), resultSpec),
                pageRequest).getContent();
        log.info("Запрос мероприятий с фильтром '{}'. Размер списка '{}'.", searchFilter, events.size());
        return events;
    }

    /**
     * Получить полную информацию о мероприятии по его идентификатору. Мероприятие должно быть опубликовано.
     *
     * @param id    идентификатор мероприятия для поиска
     * @param views количество просмотров мероприятия
     * @return найденное мероприятие
     */
    @Override
    public Event getFullEventInfoById(Long id, Long views) {
        Event event = getEvent(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Мероприятие с идентификатором '" + id + "' не опубликовано. Состояние: '" + event.getState() + "'");
        }
        event.setViews(views);
        eventRepository.save(event);
        log.info("Запрос полной информации о мероприятии с идентификатором '{}'.", id);
        return event;
    }

    /**
     * Найти полную информацию о мероприятиях согласно фильтру. Если ничего не найдено, возвращает пустой список.
     *
     * @param searchFilter фильтр поиска
     * @param from         первый элемент для отображения
     * @param size         количество элементов для отображения
     * @return найденные мероприятия
     */
    @Override
    public List<Event> getFullEventsInfoByAdmin(EventAdminSearchFilter searchFilter, Long from, Integer size) {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Specification<Event>> specifications = eventAdminSearchFilterToSpecifications(searchFilter);
        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElse(null),
                pageRequest).getContent();
        log.info("Запрос полной информации о мероприятиях администратором с фильтром '{}'. Размер списка '{}'.", searchFilter, events.size());
        return events;
    }

    /**
     * Изменить параметры и статус мероприятия. Мероприятие должно иметь состояние 'PENDING', чтобы быть измененным. Только неопубликованное мероприятие может
     * быть отменено.
     *
     * @param eventId       идентификатор мероприятия для изменения
     * @param updateRequest параметры мероприятия для обновления
     * @return обновленное мероприятие
     */
    @Override
    @Transactional
    public Event updateEventByAdmin(Long eventId, EventUpdateRequest updateRequest) {
        Event event = getEvent(eventId);
        eventMapper.updateEvent(updateRequest, event);
        updateEventState(updateRequest.getStateAction(), event);
        Event savedEvent = eventRepository.save(event);
        log.info("Мероприятие с идентификатором '{}' было обновлено администратором.", eventId);
        return savedEvent;
    }

    /**
     * Добавить комментарий к мероприятию.
     *
     * @param userId  идентификатор пользователя, добавляющего комментарий
     * @param eventId идентификатор мероприятия для комментирования
     * @param comment комментарий
     * @return добавленный комментарий
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
        log.info("Пользователь с идентификатором '{}' добавил комментарий к мероприятию с идентификатором '{}'.", userId, eventId);
        return event;
    }

    /**
     * Обновить комментарий. Только автор комментария может обновить комментарий.
     *
     * @param userId        идентификатор пользователя, обновляющего комментарий
     * @param eventId       мероприятие, к которому относится комментарий, для обновления
     * @param updateComment обновить комментарий
     * @return обновленный комментарий
     */
    @Override
    public Event updateComment(Long userId, Long eventId, Comment updateComment) {
        getUser(userId);
        Comment comment = getComment(updateComment.getId());
        checkIfUserIsCommentAuthor(userId, comment);
        comment.setText(updateComment.getText());
        Comment updatedComment = commentRepository.save(comment);
        Event event = getEvent(eventId);
        log.info("Комментарий с идентификатором '" + updatedComment.getId() + "' был обновлен.");
        return event;
    }

    /**
     * Удалить комментарий. Только автор комментария может удалить комментарий.
     *
     * @param userId    идентификатор пользователя, удаляющего комментарий
     * @param commentId идентификатор комментария для удаления
     */
    @Override
    public void deleteComment(Long userId, Long commentId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        checkIfUserIsCommentAuthor(userId, comment);
        commentRepository.deleteById(commentId);
        log.info("Комментарий с идентификатором '" + commentId + "' был удален пользователем с идентификатором '" + userId + "'.");
    }

    private void checkIfUserIsCommentAuthor(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new NotAuthorizedException("Пользователь с идентификатором '" + userId + "' не является автором комментария с идентификатором '" +
                    comment.getId() + "'.");
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findCommentById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с идентификатором '" + commentId + "' не найден."));
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
            throw new NotAuthorizedException("Невозможно опубликовать отмененное мероприятие.");
        }
    }

    private void checkIfEventIsAlreadyPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new NotAuthorizedException("Мероприятие уже опубликовано.");
        }
    }

    private Event getEvent(Long id) {
        return eventRepository.findFullEventById(id)
                .orElseThrow(() -> new NotFoundException("Мероприятие с идентификатором '" + id + "' не найдено."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с идентификатором '" + userId + "' не найден."));
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
                throw new IllegalArgumentException("Сортировка '" + eventSort + "еще не поддерживается.");
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
