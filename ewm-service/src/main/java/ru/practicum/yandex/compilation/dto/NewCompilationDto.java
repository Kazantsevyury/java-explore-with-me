package ru.practicum.yandex.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCompilationDto {

    private List<Long> events;

    private boolean pinned;

    @NotBlank(message = "Заголовок не может быть пустым и должен содержать от 1 до 50 символов.")
    @Size(min = 1, max = 50, message = "Заголовок не может быть пустым и должен содержать от 1 до 50 символов.")
    private String title;
}
