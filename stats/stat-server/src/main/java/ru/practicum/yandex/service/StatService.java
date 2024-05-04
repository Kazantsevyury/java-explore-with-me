package ru.practicum.yandex.service;

import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    EndpointHit methodHit(EndpointHit endpointHitDto);

    List<ViewStats> viewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
