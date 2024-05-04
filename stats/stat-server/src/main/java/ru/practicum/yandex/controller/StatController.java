package ru.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
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

import static org.springframework.http.HttpStatus.CREATED;

/**
 * Контроллер для обработки статистики посещений и хитов эндпоинтов.
 */
@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    /**
     * Записывает хит в системе.
     * @param endpointHitDto DTO хита для регистрации.
     * @return DTO созданного хита.
     */
    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto recordEndpointHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto);
        return endpointHitMapper.toDto(statService.methodHit(endpointHit));
    }

    /**
     * Возвращает статистику просмотров за указанный временной интервал.
     * @param start Начало интервала.
     * @param end Конец интервала.
     * @param uris Список URI для фильтрации (необязательно).
     * @param unique Признак уникальности посещений.
     * @return Список DTO статистики просмотров.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> getStatistics(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime decodedStart = LocalDateTime.parse(start);
        LocalDateTime decodedEnd = LocalDateTime.parse(end);
        validateDates(decodedStart, decodedEnd);
        return viewStatsMapper.toDtoList(statService.viewStats(decodedStart, decodedEnd, uris, unique));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IncorrectDateIntervalException("Wrong date interval. End date should be after start date.");
        }
    }
}
