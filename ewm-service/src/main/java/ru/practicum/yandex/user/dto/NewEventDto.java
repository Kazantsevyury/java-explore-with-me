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

    @NotBlank(message = "Аннотация не должна быть пустой и содержать от 20 до 2000 символов.")
    @Size(min = 20, max = 2000, message = "Аннотация не должна быть пустой и содержать от 20 до 2000 символов.")
    private String annotation;

    @NotNull(message = "Событие должно иметь категорию.")
    @JsonAlias("category")
    private Long categoryId;

    @NotBlank(message = "Описание не должно быть пустым и содержать от 20 до 7000 символов.")
    @Size(min = 20, max = 7000, message = "Описание не должно быть пустым и содержать от 20 до 7000 символов.")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ValidEventStart(message = "Дата события должна быть не менее чем через 2 часа от текущего времени.")
    private LocalDateTime eventDate;

    @NotBlank(message = "Название не должно быть пустым и содержать от 3 до 120 символов.")
    @Size(min = 3, max = 120, message = "Название не должно быть пустым и содержать от 3 до 120 символов.")
    private String title;

    @NotNull(message = "Местоположение должно быть указано.")
    private LocationDto location;

    private boolean paid;

    @PositiveOrZero(message = "Количество участников должно быть положительным или нулевым.")
    private int participantLimit;

    private boolean requestModeration = true;
}
