package ru.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.exception.IncorrectDateIntervalException;
import ru.practicum.yandex.mapper.EndpointHitMapper;
import ru.practicum.yandex.mapper.ViewStatsMapper;
import ru.practicum.yandex.service.StatService;
import ru.practicum.yandex.model.EndpointHit;

import javax.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * Контроллер для обработки статистики посещений и регистрации хитов.
 */
@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    /**
     * Записывает хит в систему.
     *
     * @param endpointHitDto объект DTO хита для регистрации.
     * @return DTO созданного хита.
     */
    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto);
        return endpointHitMapper.toDto(statService.methodHit(endpointHit));
    }

    /**
     * Возвращает статистику посещений за указанный временной интервал.
     *
     * @param start  начало интервала в формате "yyyy-MM-dd HH:mm:ss".
     * @param end    конец интервала в формате "yyyy-MM-dd HH:mm:ss".
     * @param uris   список URI для фильтрации (необязательно).
     * @param unique признак уникальности посетителей.
     * @return список DTO статистики посещений.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime decodedStart = decodeLocalDateTime(start);
        LocalDateTime decodedEnd = decodeLocalDateTime(end);
        validateDates(decodedStart, decodedEnd);
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
