package ru.practicum.yandex.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.events.dto.LocationDto;
import ru.practicum.yandex.user.validation.ValidEventStart;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewEventDto {

    @NotBlank(message = "Annotation must not be blank or empty and contain between 20 and 2000 characters.")
    @Size(min = 20, max = 2000, message = "Annotation must not be blank or empty and contain between 20 and 2000 characters.")
    private String annotation;

    @NotNull(message = "Event must have category.")
    @JsonAlias("category")
    private Long categoryId;

    @NotBlank(message = "Description must not be blank or empty and contain between 20 and 7000 characters.")
    @Size(min = 20, max = 7000, message = "Description must not be blank or empty and contain between 20 and 7000 characters.")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ValidEventStart(message = "Date of event must be at least 2 hours later than current time.")
    private LocalDateTime eventDate;

    @NotBlank(message = "Title must not be blank or empty and contain between 3 and 120 characters.")
    @Size(min = 3, max = 120, message = "Title must not be blank or empty and contain between 3 and 120 characters.")
    private String title;

    @NotNull(message = "Location must be specified.")
    private LocationDto location;

    private boolean paid;

    @PositiveOrZero(message = "Number of participants must be positive or zero.")
    private int participantLimit;

    private boolean requestModeration = true;
}
