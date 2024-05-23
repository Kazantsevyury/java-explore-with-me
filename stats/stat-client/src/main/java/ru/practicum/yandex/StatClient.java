package ru.practicum.yandex;

import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Интерфейс для взаимодействия с сервисом статистики.
 */
public interface StatClient {

    /**
     * Отправляет информацию о посещении эндпоинта.
     *
     * @param endpointHitDto данные о посещении
     * @return данные о посещении, сохраненные в сервисе статистики
     */
    EndpointHitDto methodHit(EndpointHitDto endpointHitDto);

    /**
     * Получает статистику посещений за указанный период.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список URI для фильтрации
     * @param unique учитывать только уникальные посещения
     * @return список статистических данных
     */
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

    /**
     * Получает уникальную статистику по конкретному URI.
     *
     * @param uri URI для получения статистики
     * @return данные статистики
     */
    ViewStatsDto getUniqueIpStatsForUri(String uri);
}
