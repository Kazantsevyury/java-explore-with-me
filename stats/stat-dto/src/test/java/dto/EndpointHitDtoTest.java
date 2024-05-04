package dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ru.practicum.yandex.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


class EndpointHitDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows

    void whenDateTimeWithoutFormat_ShouldReturnFormattedJson() {
        objectMapper.registerModule(new JavaTimeModule());

        EndpointHitDto endpointHitDto = EndpointHitDto.of(null,"ewm-main-ru.practicum.yandex.service", "/events/1", "192.163.0.1",
                LocalDateTime.of(2022, 9,
                6, 11, 0, 23));

        String json = "{\n" +
                "  \"app\": \"ewm-main-ru.practicum.yandex.service\",\n" +
                "  \"uri\": \"/events/1\",\n" +
                "  \"ip\": \"192.163.0.1\",\n" +
                "  \"timestamp\": \"2022-09-06 11:00:23\"\n" +
                "}";

        EndpointHitDto result = objectMapper.readValue(json, EndpointHitDto.class);

        assertEquals(endpointHitDto, result);
    }
}