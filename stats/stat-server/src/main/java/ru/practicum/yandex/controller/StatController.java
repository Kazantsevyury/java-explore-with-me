package ru.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.exception.IncorrectDateIntervalException;
import ru.practicum.yandex.mapper.EndpointHitMapper;
import ru.practicum.yandex.mapper.ViewStatsMapper;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер для сбора статистики и обработки хитов.
 */
@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    /**
     * Регистрирует хит в системе.
     *
     * @param endpointHitDto объект DTO хита для регистрации.
     * @return DTO созданного хита.
     */
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto);
        return endpointHitMapper.toDto(statService.methodHit(endpointHit));
    }

    /**
     * Получает статистику посещений за указанный период.
     *
     * @param start  начало периода в формате "yyyy-MM-dd HH:mm:ss".
     * @param end    конец периода в формате "yyyy-MM-dd HH:mm:ss".
     * @param uris   список URI для фильтрации (опционально).
     * @param unique флаг уникальности посетителей.
     * @return список DTO статистики посещений.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        validateDates(start, end);
        return viewStatsMapper.toDtoList(statService.viewStats(start, end, uris, unique));
    }

    /**
     * Проверяет корректность временного интервала.
     *
     * @param start начальная дата.
     * @param end   конечная дата.
     * @throws IncorrectDateIntervalException если начальная дата позже конечной.
     */
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IncorrectDateIntervalException("Wrong date interval. End date should be after start date.");
        }
    }
}
