package ru.practicum.yandex.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;
import ru.practicum.yandex.exception.InvalidDateRangeException;
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

/**
 * Контроллер для управления статистикой и регистрации хитов в приложении.
 */
@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    /**
     * Регистрирует хит для указанного endpoint.
     * @param endpointHitDto DTO хита для регистрации.
     * @return DTO зарегистрированного хита.
     */
    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toEndpointModel(endpointHitDto);
        return endpointHitMapper.toEndpointDto(statService.methodHit(endpointHit));
    }

    /**
     * Возвращает статистику просмотров для указанного временного интервала и списка URI.
     * @param start Начало временного интервала.
     * @param end Конец временного интервала.
     * @param uris Список URI, по которым нужно собрать статистику (опционально).
     * @param unique Если true, то возвращать только уникальные просмотры.
     * @return Список DTO статистики просмотров.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime decodedStart = decodeLocalDateTime(start);
        LocalDateTime decodedEnd = decodeLocalDateTime(end);
        validateDates(decodedStart, decodedEnd);
        return viewStatsMapper.toViewStatsDtoList(statService.viewStats(decodedStart, decodedEnd, uris, unique));
    }

    /**
     * Проверяет, что начальная дата не позднее конечной даты.
     * @param start Начальная дата интервала.
     * @param end Конечная дата интервала.
     * @throws InvalidDateRangeException если начальная дата позже конечной.
     */
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new InvalidDateRangeException("Неверный интервал дат. Дата окончания должна быть после даты начала.");
        }
    }

    /**
     * Декодирует URL-кодированную строку даты и времени и преобразует ее в объект LocalDateTime.
     * @param encodedDateTime URL-кодированная строка даты и времени.
     * @return Объект LocalDateTime.
     */
    private LocalDateTime decodeLocalDateTime(String encodedDateTime) {
        String decodedDateTime = URLDecoder.decode(encodedDateTime, StandardCharsets.UTF_8);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(decodedDateTime, dateTimeFormatter);
    }
}
