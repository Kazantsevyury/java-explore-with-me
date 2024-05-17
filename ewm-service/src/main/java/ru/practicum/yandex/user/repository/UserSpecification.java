package ru.practicum.yandex.user.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.yandex.user.model.User;

import java.util.List;

@UtilityClass
public class UserSpecification {

    public static Specification<User> idIn(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("id")).value(ids);
    }
}
