package ru.practicum.yandex.events.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.user.dto.StateAction;
import ru.practicum.yandex.user.validation.ValidEventStart;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventUpdateRequest {

    @Size(min = 20, max = 2000, message = "Annotation must not be blank or empty and contain between 20 and 2000 characters.")
    private String annotation;

    @Positive(message = "Category id must be positive.")
    @JsonAlias("category")
    private Long categoryId;

    @Size(min = 20, max = 7000, message = "Description must not be blank or empty and contain between 20 and 7000 characters.")
    private String description;

    @ValidEventStart(message = "Date of event must be at least 2 hours later than current time.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Size(min = 3, max = 120, message = "Title must not be blank or empty and contain between 3 and 120 characters.")
    private String title;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Participant limit must be positive or zero.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;
}
