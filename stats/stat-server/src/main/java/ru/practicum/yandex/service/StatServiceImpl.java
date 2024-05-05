package ru.practicum.yandex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Override
    public EndpointHit methodHit(EndpointHit endpointHit) {
        return statRepository.save(endpointHit);
    }

    @Override
    public List<ViewStats> viewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return unique ? collectUniqueVisitorStats(start, end, uris) : collectGeneralStats(start, end, uris);
    }

    private List<ViewStats> collectGeneralStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return (uris == null) ? statRepository.findStats(start, end) : statRepository.findStatsFromUrlList(start, end, uris);
    }

    private List<ViewStats> collectUniqueVisitorStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return (uris == null) ? statRepository.findStatsWithUniqueIps(start, end) : statRepository.findStatsFromListWithUniqueIps(start, end, uris);
    }
}
