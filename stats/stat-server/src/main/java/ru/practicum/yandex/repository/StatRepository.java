package ru.practicum.yandex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHit, Long>, JpaSpecificationExecutor<EndpointHit> {

    @Query("SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(eh.ip)) FROM EndpointHit eh " +
            "WHERE eh.timestamp > ?1 AND eh.timestamp < ?2 AND eh.uri IN (?3) GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStatsFromUrlList(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT(eh.ip))) FROM EndpointHit eh " +
            "WHERE eh.timestamp > ?1 AND eh.timestamp < ?2 AND eh.uri IN (?3) GROUP BY eh.app, eh.uri " +
            "ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStatsFromUriListWithUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(eh.ip)) FROM EndpointHit eh " +
            "WHERE eh.timestamp > ?1 AND eh.timestamp < ?2 GROUP BY eh.app, eh.uri ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT(eh.ip))) FROM EndpointHit eh " +
            "WHERE eh.timestamp > ?1 AND eh.timestamp < ?2 GROUP BY eh.app, eh.uri ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStatsWithUniqueIps(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(DISTINCT(eh.ip))) FROM EndpointHit eh WHERE eh.uri = ?1 " +
            "GROUP BY eh.app, eh.uri")
    ViewStats findStatsForUriWithUniqueIps(String uri);
}
