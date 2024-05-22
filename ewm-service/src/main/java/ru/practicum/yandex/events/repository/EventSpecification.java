package ru.practicum.yandex.events.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Класс EventSpecification предоставляет спецификации для фильтрации событий в базе данных.
 * Содержит статические методы для создания различных критериев поиска.
 */
public class EventSpecification {

    private EventSpecification() {
        // Приватный конструктор, чтобы предотвратить создание экземпляра класса
    }

    /**
     * Фильтр по тексту в аннотации или описании события (без учета регистра).
     *
     * @param text текст для поиска
     * @return спецификация для поиска событий
     */
    public static Specification<Event> textInAnnotationOrDescriptionIgnoreCase(String text) {
        if (text == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                                "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + text.toLowerCase() + "%")
                );
    }

    /**
     * Фильтр по идентификаторам категорий.
     *
     * @param categoryIds список идентификаторов категорий
     * @return спецификация для поиска событий
     */
    public static Specification<Event> categoriesIdIn(List<Long> categoryIds) {
        if (categoryIds == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(categoryIds);
    }

    /**
     * Фильтр по признаку платного события.
     *
     * @param isPaid признак платного события
     * @return спецификация для поиска событий
     */
    public static Specification<Event> isPaid(Boolean isPaid) {
        if (isPaid == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), isPaid);
    }

    /**
     * Фильтр по диапазону дат проведения события.
     *
     * @param startRange начало диапазона
     * @param endRange конец диапазона
     * @return спецификация для поиска событий
     */
    public static Specification<Event> eventDateInRange(LocalDateTime startRange, LocalDateTime endRange) {
        if (startRange == null || endRange == null) {
            return eventDateAfter(LocalDateTime.now());
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventDate"), startRange, endRange);
    }

    /**
     * Фильтр по дате проведения события после указанного времени.
     *
     * @param dateTime дата и время
     * @return спецификация для поиска событий
     */
    public static Specification<Event> eventDateAfter(LocalDateTime dateTime) {
        if (dateTime == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("eventDate"), dateTime);
    }

    /**
     * Фильтр по статусу события.
     *
     * @param eventState статус события
     * @return спецификация для поиска событий
     */
    public static Specification<Event> eventStatusEquals(EventState eventState) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), eventState);
    }

    /**
     * Фильтр по списку статусов событий.
     *
     * @param states список статусов событий
     * @return спецификация для поиска событий
     */
    public static Specification<Event> eventStatusIn(List<EventState> states) {
        if (states == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(states);
    }

    /**
     * Фильтр по списку идентификаторов инициаторов событий.
     *
     * @param userIds список идентификаторов пользователей
     * @return спецификация для поиска событий
     */
    public static Specification<Event> initiatorIdIn(List<Long> userIds) {
        if (userIds == null) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(userIds);
    }

    /**
     * Фильтр по доступным событиям.
     *
     * @param isAvailable признак доступности события
     * @return спецификация для поиска событий
     */
    public static Specification<Event> isAvailable(boolean isAvailable) {
        if (!isAvailable) {
            return Specification.where(null);
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("participantLimit"),
                root.get("numberOfParticipants"));
    }

    /**
     * Сортировка по количеству комментариев.
     *
     * @param spec спецификация для поиска событий
     * @return спецификация для сортировки событий
     */
    public static Specification<Event> orderByNumberOfComments(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(
                            criteriaBuilder.size(root.get("comments"))
                    ));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    /**
     * Сортировка по идентификатору.
     *
     * @param spec спецификация для поиска событий
     * @return спецификация для сортировки событий
     */
    public static Specification<Event> orderById(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.asc(root.get("id")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    /**
     * Сортировка по количеству просмотров.
     *
     * @param spec спецификация для поиска событий
     * @return спецификация для сортировки событий
     */
    public static Specification<Event> orderByViews(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("views")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    /**
     * Сортировка по дате проведения события.
     *
     * @param spec спецификация для поиска событий
     * @return спецификация для сортировки событий
     */
    public static Specification<Event> orderByEventDate(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("eventDate")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
