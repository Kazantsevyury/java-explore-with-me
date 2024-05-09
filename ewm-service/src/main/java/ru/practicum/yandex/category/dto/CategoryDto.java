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

    @NotBlank(message = "Name must no be blank or empty and must have length between 1 and 50 characters.")
    @Size(min = 1, max = 50, message = "Name must no be blank or empty and must have length between 1 and 50 characters.")
    private String name;
}
