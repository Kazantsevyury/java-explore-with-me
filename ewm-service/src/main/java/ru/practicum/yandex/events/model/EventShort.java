package ru.practicum.yandex.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventShort {

    private Long id;

    private String annotation;

    private CategoryDto category;

    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Boolean paid;

    private String title;

    private Long confirmedRequests;

    private Long views;
}
