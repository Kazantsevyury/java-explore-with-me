package ru.practicum.yandex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Collections;
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
    public List<ViewStats> viewStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, Boolean unique) {
        uris = uris != null ? uris : Collections.emptyList();
        if (unique) {
            return getStats(startDate, endDate, uris, true);
        } else {
            return getStats(startDate, endDate, uris, false);
        }
    }

    private List<ViewStats> getStats(LocalDateTime startDate, LocalDateTime endDate, List<String> uris, boolean unique) {
        if (unique) {
            return uris.isEmpty() ?
                    statRepository.findStatsWithUniqueIps(startDate, endDate) :
                    statRepository.findStatsFromListWithUniqueIps(startDate, endDate, uris);
        } else {
            return uris.isEmpty() ?
                    statRepository.findStats(startDate, endDate) :
                    statRepository.findStatsFromUrlList(startDate, endDate, uris);
        }
    }
}
