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
        EndpointHit savedHit = statRepository.save(endpointHit);
        return savedHit;
    }

    @Override
    public List<ViewStats> viewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return getStatsFromUniqueIps(start, end, uris);
        } else {
            return getAllStats(start, end, uris);
        }
    }

    private List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            return statRepository.findStats(start, end);
        } else {
            return statRepository.findStatsFromUrlList(start, end, uris);
        }
    }

    private List<ViewStats> getStatsFromUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            return statRepository.findStatsWithUniqueIps(start, end);
        } else {
            return statRepository.findStatsFromListWithUniqueIps(start, end, uris);
        }
    }
}
