package ru.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.exception.IncorrectDateIntervalException;
import ru.practicum.yandex.mapper.EndpointHitMapper;
import ru.practicum.yandex.mapper.ViewStatsMapper;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.service.StatService;

import javax.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    private final EndpointHitMapper endpointHitMapper;

    private final ViewStatsMapper viewStatsMapper;

    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto);
        log.info("StatController uri '{}', request body '{}'.", "/hit", endpointHitDto);
        return endpointHitMapper.toDto(statService.methodHit(endpointHit));
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime decodedStart = decodeLocalDateTime(start);
        LocalDateTime decodedEnd = decodeLocalDateTime(end);
        validateDates(decodedStart, decodedEnd);
        log.info("StatController uri '{}', start = '{}', end = '{}', uris = '{}', unique = '{}'.", "/stats", start,
                end, uris, unique);
        return viewStatsMapper.toDtoList(statService.viewStats(decodedStart, decodedEnd, uris, unique));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IncorrectDateIntervalException("Wrong date interval. End date should be after start date.");
        }
    }

    private LocalDateTime decodeLocalDateTime(String encodedDateTime) {
        String decodedDateTime = URLDecoder.decode(encodedDateTime, StandardCharsets.UTF_8);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(decodedDateTime, dateTimeFormatter);
    }

}
