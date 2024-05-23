package ru.practicum.yandex.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewUserRequest {

    @NotBlank(message = "Длина имени должна быть от 2 до 250 символов.")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250 символов.")
    private String name;

    @Email(message = "Неверный формат электронной почты.")
    @Size(min = 6, max = 254, message = "Неверный формат электронной почты.")
    @NotBlank(message = "Неверный формат электронной почты.")
    private String email;
}
