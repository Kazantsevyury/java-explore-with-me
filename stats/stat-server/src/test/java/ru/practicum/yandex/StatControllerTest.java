package ru.practicum.yandex;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.yandex.controller.StatController;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.exception.IncorrectDateIntervalException;
import ru.practicum.yandex.mapper.EndpointHitMapper;
import ru.practicum.yandex.mapper.ViewStatsMapper;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.service.StatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatController.class)
class StatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatService statService;

    @MockBean
    private EndpointHitMapper endpointHitMapper;

    @MockBean
    private ViewStatsMapper viewStatsMapper;

    @Test
    @SneakyThrows
    @DisplayName("Request with all parameters should return 200")
    void viewStats_whenAllValidField_ShouldReturn200() {
        LocalDateTime start = LocalDateTime.of(2020, 11, 3, 11, 54, 22);
        LocalDateTime end = LocalDateTime.of(2020, 11, 4, 12, 34, 11);
        List<String> uris = List.of("uri1", "uri2");
        Boolean unique = true;

        ViewStats viewStats = new ViewStats("app", "uri", 4L);
        ViewStatsDto viewStatsDto = new ViewStatsDto("appDto", "uriDto", 4L);

        when(statService.viewStats(start, end, uris, unique))
                .thenReturn(List.of(viewStats));
        when(viewStatsMapper.toDtoList(List.of(viewStats)))
                .thenReturn(List.of(viewStatsDto));

        MultiValueMap<String, String> urisRequest = new LinkedMultiValueMap<>();
        urisRequest.add("uris", "uri1");
        urisRequest.add("uris", "uri2");

        mvc.perform(get("/stats")
                        .param("start", "2020-11-03+11:54:22")
                        .param("end", "2020-11-04+12:34:11")
                        .params(urisRequest)
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].app", is(viewStatsDto.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(viewStatsDto.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(4)));

        verify(statService, times(1)).viewStats(start, end, uris, unique);
        verify(viewStatsMapper, times(1)).toDtoList(List.of(viewStats));
    }

    @Test
    @SneakyThrows
    @DisplayName("Request without uris and unique params should return 200")
    void viewStats_whenAllRequiredField_ShouldReturn200() {
        LocalDateTime start = LocalDateTime.of(2020, 11, 3, 11, 54, 22);
        LocalDateTime end = LocalDateTime.of(2020, 11, 4, 12, 34, 11);

        ViewStats viewStats = new ViewStats("app", "uri", 4L);
        ViewStatsDto viewStatsDto = new ViewStatsDto("appDto", "uriDto", 4L);

        when(statService.viewStats(start, end, null, false))
                .thenReturn(List.of(viewStats));
        when(viewStatsMapper.toDtoList(List.of(viewStats)))
                .thenReturn(List.of(viewStatsDto));

        mvc.perform(get("/stats")
                        .param("start", "2020-11-03 11:54:22")
                        .param("end", "2020-11-04 12:34:11"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].app", is(viewStatsDto.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(viewStatsDto.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(4)));

        verify(statService, times(1)).viewStats(start, end, null, false);
    }

    @Test
    @SneakyThrows
    @DisplayName("Request with wrong format should throw DateTimeParseException")
    void viewStats_whenWrongDateTimeFormat_ShouldThrowDateTimeParseException() {
        LocalDateTime start = LocalDateTime.of(2020, 11, 3, 11, 54, 22);
        LocalDateTime end = LocalDateTime.of(2020, 11, 4, 12, 34, 11);

        ViewStats viewStats = new ViewStats("app", "uri", 4L);

        when(statService.viewStats(start, end, null, null))
                .thenReturn(List.of(viewStats));

        mvc.perform(get("/stats")
                        .param("start", "2020-11-03 11-54-22")
                        .param("end", "2020-11-04 12-34-11"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof
                        DateTimeParseException));

        verify(statService, never()).viewStats(any(), any(), any(), any());
        verify(viewStatsMapper, never()).toDtoList(anyList());
    }

    @Test
    @SneakyThrows
    @DisplayName("Request with wrong date interval should throw IncorrectDateIntervalException")
    void viewStats_whenWrongDateTimeInterval_ShouldThrowIncorrectDateIntervalException() {
        LocalDateTime start = LocalDateTime.of(2020, 11, 4, 12, 34, 11);
        LocalDateTime end = LocalDateTime.of(2020, 11, 3, 11, 54, 22);

        ViewStats viewStats = new ViewStats("app", "uri", 4L);

        when(statService.viewStats(start, end, null, null))
                .thenReturn(List.of(viewStats));

        mvc.perform(get("/stats")
                        .param("start", "2020-11-04 12:34:11")
                        .param("end", "2020-11-03 11:54:22"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof
                        IncorrectDateIntervalException));

        verify(statService, never()).viewStats(start, end, null, null);
        verify(viewStatsMapper, never()).toDtoList(anyList());
    }

    @Test
    @SneakyThrows
    @DisplayName("When all fields are valid, should return 201")
    void methodHit_whenAllFieldsAreValid_ShouldReturn201() {
        EndpointHit endpointHit = EndpointHit.of(null, "app", "/uri/er", "123.43.23.12",
                LocalDateTime.of(2022, 10, 23, 11, 34, 44));
        EndpointHitDto endpointHitDto = EndpointHitDto.of(null, "appDto", "/uri/erDto", "124.43.23.12",
                LocalDateTime.of(2024, 10, 23, 11, 34, 44));

        when(statService.methodHit(endpointHit))
                .thenReturn(endpointHit);
        when(endpointHitMapper.toModel(endpointHitDto))
                .thenReturn(endpointHit);
        when(endpointHitMapper.toDto(endpointHit))
                .thenReturn(endpointHitDto);

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        mvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(endpointHitDto.getId())))
                .andExpect(jsonPath("$.app", is(endpointHitDto.getApp())))
                .andExpect(jsonPath("$.uri", is(endpointHitDto.getUri())))
                .andExpect(jsonPath("$.ip", is(endpointHitDto.getIp())))
                .andExpect(jsonPath("$.timestamp", is(endpointHitDto.getTimestamp().format(pattern))));

        verify(statService, times(1)).methodHit(endpointHit);
        verify(endpointHitMapper, times(1)).toModel(endpointHitDto);
        verify(endpointHitMapper, times(1)).toDto(endpointHit);
    }

    @Test
    @SneakyThrows
    @DisplayName("Attempt to save EndpointHit with wrong ip format")
    void methodHit_WhenWrongIpFormat_ShouldThrowConstraintViolationException() {
        EndpointHitDto endpointHitDto = EndpointHitDto.of(null, "app", "/uri/er", "ip",
                LocalDateTime.of(2022, 10, 23, 11, 34, 44));

        mvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

        verify(statService, never()).methodHit(any());
    }
}