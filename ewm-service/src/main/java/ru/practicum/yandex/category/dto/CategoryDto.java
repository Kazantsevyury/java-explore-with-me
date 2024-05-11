package ru.practicum.yandex.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Название не должно быть пустым и должно содержать от 1 до 50 символов.")
    @Size(min = 1, max = 50, message = "Название не должно быть пустым и должно содержать от 1 до 50 символов.")
    private String name;
}
