package ru.practicum.yandex.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.events.model.Location;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewEvent {

    private String annotation;

    private Long categoryId;

    private String description;

    private LocalDateTime eventDate;

    private String title;

    private Location location;

    private boolean paid;

    private int participantLimit;

    private boolean requestModeration;
}
