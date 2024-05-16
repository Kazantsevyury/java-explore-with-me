package ru.practicum.yandex;

import ru.practicum.yandex.dto.EndpointHitDto;
import ru.practicum.yandex.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatClient {

    EndpointHitDto methodHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

    ViewStatsDto getUniqueIpStatsForUri(String uri);
}
