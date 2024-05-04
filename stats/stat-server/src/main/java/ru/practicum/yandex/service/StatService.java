package ru.practicum.yandex.service;

import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    // Метод для обработки информации о запросах к конечной точке
    EndpointHit methodHit(EndpointHit endpointHitDto);

    // Метод для получения статистики просмотров за определенный период времени
    // с возможностью фильтрации по списку URI и уникальности IP-адресов
    List<ViewStats> viewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
