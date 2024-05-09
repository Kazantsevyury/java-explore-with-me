package ru.practicum.yandex.events.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventSpecification {

    public static Specification<Event> textInAnnotationOrDescriptionIgnoreCase(String text) {
        if (text == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                                "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + text.toLowerCase() + "%")
                );
    }

    public static Specification<Event> categoriesIdIn(List<Long> categoryIds) {
        if (categoryIds == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(categoryIds);
    }

    public static Specification<Event> isPaid(Boolean isPaid) {
        if (isPaid == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), isPaid);
    }

    public static Specification<Event> eventDateInRange(LocalDateTime startRange, LocalDateTime endRange) {
        if (startRange == null || endRange == null) {
            return eventDateAfter(LocalDateTime.now());
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventDate"), startRange, endRange);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("eventDate"), dateTime);
    }

    public static Specification<Event> eventStatusEquals(EventState eventState) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("state"), eventState);
    }

    public static Specification<Event> eventStatusIn(List<EventState> states) {
        if (states == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(states);
    }

    public static Specification<Event> initiatorIdIn(List<Long> userIds) {
        if (userIds == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(userIds);
    }

    public static Specification<Event> isAvailable(boolean isAvailable) {
        if (!isAvailable) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("participantLimit"),
                root.get("numberOfParticipants"));
    }

    public static Specification<Event> orderByNumberOfComments(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(
                            criteriaBuilder.size(root.get("comments"))
                    ));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<Event> orderById(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.asc(root.get("id")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<Event> orderByViews(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("views")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }

    public static Specification<Event> orderByEventDate(Specification<Event> spec) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(
                    criteriaBuilder.desc(root.get("eventDate")));
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
