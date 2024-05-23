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
import ru.practicum.yandex.model.ViewStats;
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

    /**
     * Добавление информации о хите.
     *
     * @param endpointHitDto данные хита
     * @return сохраненный хит
     */
    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto);
        log.info("Добавление обращения к методу, тело запроса '{}'.", endpointHitDto);
        EndpointHit savedHit = statService.methodHit(endpointHit);
        return endpointHitMapper.toDto(savedHit);
    }

    /**
     * Получение статистики просмотров.
     *
     * @param start   начало интервала
     * @param end     конец интервала
     * @param uris    список URI для фильтрации
     * @param unique  учитывать только уникальные запросы
     * @return список статистики просмотров
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> viewStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {
        LocalDateTime decodedStart = decodeLocalDateTime(start);
        LocalDateTime decodedEnd = decodeLocalDateTime(end);
        validateDates(decodedStart, decodedEnd);
        log.info("Запрос статистики, начало = '{}', конец = '{}', URIs = '{}', уникальность = '{}'.", start, end, uris, unique);
        List<ViewStats> statsList = statService.viewStats(decodedStart, decodedEnd, uris, unique);
        return viewStatsMapper.toDtoList(statsList);
    }

    /**
     * Получение статистики по уникальным IP-адресам для указанного URI.
     *
     * @param uri URI для запроса
     * @return статистика по уникальным IP-адресам
     */
    @GetMapping("/statistic")
    public ViewStatsDto viewUniqueStatsForUri(@RequestParam String uri) {
        log.info("Запрос статистики по уникальным IP-адресам для URI '{}'.", uri);
        ViewStats stats = statService.viewStatsForSingleUriWithUniqueIps(uri);
        return viewStatsMapper.toDto(stats);
    }

    /**
     * Проверка корректности интервала дат.
     *
     * @param start начало интервала
     * @param end   конец интервала
     */
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IncorrectDateIntervalException("Неправильный интервал даты. Дата окончания должна быть после даты начала.");
        }
    }

    /**
     * Декодирование даты и времени из строки.
     *
     * @param encodedDateTime закодированная строка даты и времени
     * @return декодированное значение LocalDateTime
     */
    private LocalDateTime decodeLocalDateTime(String encodedDateTime) {
        String decodedDateTime = URLDecoder.decode(encodedDateTime, StandardCharsets.UTF_8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(decodedDateTime, formatter);
    }
}
