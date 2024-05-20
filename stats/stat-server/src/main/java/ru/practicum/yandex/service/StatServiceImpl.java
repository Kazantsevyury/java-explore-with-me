package ru.practicum.yandex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    /**
     * Метод для регистрации хита конечной точки.
     *
     * @param endpointHit информация о хите
     * @return сохраненный хит
     */
    @Override
    public EndpointHit methodHit(EndpointHit endpointHit) {
        EndpointHit savedHit = statRepository.save(endpointHit);
        log.info("Зарегистрирован хит конечной точки с идентификатором '{}'.", savedHit.getId());
        return savedHit;
    }

    /**
     * Метод для получения статистики просмотров.
     *
     * @param start  начало периода
     * @param end    конец периода
     * @param uris   список URI для фильтрации
     * @param unique учитывать только уникальные запросы
     * @return список статистики просмотров
     */
    @Override
    public List<ViewStats> viewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return getStatsFromUniqueIps(start, end, uris);
        } else {
            return getAllStats(start, end, uris);
        }
    }

    /**
     * Метод для получения статистики по уникальным IP-адресам для одного URI.
     *
     * @param uri URI для запроса
     * @return статистика по уникальным IP-адресам
     */
    @Override
    public ViewStats viewStatsForSingleUriWithUniqueIps(String uri) {
        log.info("Запрос статистики для уникальных IP-адресов для URI '{}'.", uri);
        return statRepository.findStatsForUriWithUniqueIps(uri);
    }

    /**
     * Метод для получения статистики для всех URI.
     *
     * @param start начало периода
     * @param end   конец периода
     * @param uris  список URI для фильтрации
     * @return список статистики просмотров
     */
    private List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            log.info("Запрос статистики между '{}' и '{}' для всех URI.", start, end);
            return statRepository.findStats(start, end);
        } else {
            log.info("Запрос статистики между '{}' и '{}' для URI '{}'.", start, end, uris);
            return statRepository.findStatsFromUrlList(start, end, uris);
        }
    }

    /**
     * Метод для получения статистики по уникальным IP-адресам для списка URI.
     *
     * @param start начало периода
     * @param end   конец периода
     * @param uris  список URI для фильтрации
     * @return список статистики просмотров
     */
    private List<ViewStats> getStatsFromUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null) {
            log.info("Запрос статистики с уникальными IP-адресами между '{}' и '{}' для всех URI.", start, end);
            return statRepository.findStatsWithUniqueIps(start, end);
        } else {
            log.info("Запрос статистики с уникальными IP-адресами между '{}' и '{}' для URI '{}'.", start, end, uris);
            return statRepository.findStatsFromUriListWithUniqueIps(start, end, uris);
        }
    }
}
