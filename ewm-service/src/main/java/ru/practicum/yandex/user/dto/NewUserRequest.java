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

    @NotBlank(message = "Name length must be between 2 and 250 characters.")
    @Size(min = 2, max = 250, message = "Name length must be between 2 and 250 characters.")
    private String name;

    @Email(message = "Wrong email format.")
    @Size(min = 6, max = 254, message = "Wrong email format.")
    @NotBlank(message = "Wrong email format.")
    private String email;
}
