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

    @NotBlank(message = "Title can not be blank and must contain between 1 and 50 characters.")
    @Size(min = 1, max = 50, message = "Title can not be blank and must contain between 1 and 50 characters.")
    private String title;
}
