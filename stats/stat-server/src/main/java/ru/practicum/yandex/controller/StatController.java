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
@RequiredArgsConstructor // Lombok аннотация для конструктора с обязательными полями
public class StatController {

    private final StatService statService; // Сервис для обработки статистики
    private final EndpointHitMapper endpointHitMapper; // Маппер для преобразования DTO в модель и обратно
    private final ViewStatsMapper viewStatsMapper; // Маппер для преобразования списка моделей в список DTO

    /**
     * Регистрирует хит для указанного endpoint.
     * @param endpointHitDto DTO хита для регистрации.
     * @return DTO зарегистрированного хита.
     */
    @PostMapping("/hit")
    @ResponseStatus(CREATED) // Отправка HTTP статуса 201 (Created)
    public EndpointHitDto methodHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = endpointHitMapper.toModel(endpointHitDto); // Преобразование DTO в модель
        return endpointHitMapper.toDto(statService.methodHit(endpointHit)); // Возврат преобразованного DTO после сохранения
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
        LocalDateTime decodedStart = decodeLocalDateTime(start); // Декодирование и парсинг начальной даты
        LocalDateTime decodedEnd = decodeLocalDateTime(end); // Декодирование и парсинг конечной даты
        validateDates(decodedStart, decodedEnd); // Проверка корректности интервала дат
        return viewStatsMapper.toDtoList(statService.viewStats(decodedStart, decodedEnd, uris, unique)); // Возврат списка DTO статистики
    }

    /**
     * Проверяет, что начальная дата не позднее конечной даты.
     * @param start Начальная дата интервала.
     * @param end Конечная дата интервала.
     * @throws IncorrectDateIntervalException если начальная дата позже конечной.
     */
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IncorrectDateIntervalException("Wrong date interval. End date should be after start date.");
        }
    }

    /**
     * Декодирует URL-кодированную строку даты и времени и преобразует ее в объект LocalDateTime.
     * @param encodedDateTime URL-кодированная строка даты и времени.
     * @return Объект LocalDateTime.
     */
    private LocalDateTime decodeLocalDateTime(String encodedDateTime) {
        String decodedDateTime = URLDecoder.decode(encodedDateTime, StandardCharsets.UTF_8); // Декодирование строки
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Форматтер даты и времени
        return LocalDateTime.parse(decodedDateTime, dateTimeFormatter); // Преобразование декодированной строки в LocalDateTime
    }
}
