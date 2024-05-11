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

    @Size(min = 20, max = 2000, message = "Аннотация не должна быть пустой и должна содержать от 20 до 2000 символов.")
    private String annotation;

    @Positive(message = "Идентификатор категории должен быть положительным.")
    @JsonAlias("category")
    private Long categoryId;

    @Size(min = 20, max = 7000, message = "Описание не должно быть пустым и должно содержать от 20 до 7000 символов.")
    private String description;

    @ValidEventStart(message = "Дата мероприятия должна быть не менее чем через 2 часа от текущего времени.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Size(min = 3, max = 120, message = "Название не должно быть пустым и должно содержать от 3 до 120 символов.")
    private String title;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников должен быть нулевым или положительным.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;
}
