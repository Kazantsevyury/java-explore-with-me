package ru.practicum.yandex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.validation.ValidIPv4;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EndpointHitDto {

    private Long id;

    private String app;

    private String uri;

    @ValidIPv4
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
