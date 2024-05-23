package ru.practicum.yandex.events.dto;

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
public class AddCommentDto {

    @NotBlank(message = "Text must not be blank or empty and must have between 1 and 2000 characters.")
    @Size(min = 1, max = 2000, message = "Text must not be blank or empty and must have between 1 and 2000 characters.")
    private String text;
}
