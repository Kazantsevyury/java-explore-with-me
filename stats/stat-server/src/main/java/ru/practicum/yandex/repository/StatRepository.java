package ru.practicum.yandex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHit, Long> {

    static final String BASE_STATS_QUERY =
            "SELECT new ru.practicum.yandex.model.ViewStats(eh.app, eh.uri, COUNT(eh.ip)) " +
                    "FROM EndpointHit eh WHERE eh.timestamp > :start AND eh.timestamp < :end ";

    @Query(BASE_STATS_QUERY +
            "AND eh.uri IN :uris " +
            "GROUP BY eh.app, eh.uri ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStatsFromUrlList(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("uris") List<String> uris);

    @Query(BASE_STATS_QUERY +
            "AND eh.uri IN :uris " +
            "GROUP BY eh.app, eh.uri ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStats> findStatsFromListWithUniqueIps(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("uris") List<String> uris);

    @Query(BASE_STATS_QUERY +
            "GROUP BY eh.app, eh.uri ORDER BY COUNT(eh.ip) DESC")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query(BASE_STATS_QUERY +
            "GROUP BY eh.app, eh.uri ORDER BY COUNT(DISTINCT eh.ip) DESC")
    List<ViewStats> findStatsWithUniqueIps(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
}
