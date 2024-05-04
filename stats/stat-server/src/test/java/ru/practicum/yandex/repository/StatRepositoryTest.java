package ru.practicum.yandex.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class StatRepositoryTest {

    private EndpointHit savedEndpointHit1;
    private EndpointHit savedEndpointHit2;
    private EndpointHit savedEndpointHit3;
    private EndpointHit savedEndpointHit4;

    @Autowired
    private StatRepository statRepository;

    @BeforeEach
    void init() {
        savedEndpointHit1 = statRepository.save(EndpointHit.of(null, "app1", "/app1", "1.1.1.1",
                LocalDateTime.of(2022, 9, 21, 11, 23, 44)));
        savedEndpointHit2 = statRepository.save(EndpointHit.of(null, "app2", "/app2", "2.1.1.1",
                LocalDateTime.of(2022, 9, 24, 11, 23, 44)));
        savedEndpointHit3 = statRepository.save(EndpointHit.of(null, "app2", "/app2", "1.1.1.1",
                LocalDateTime.of(2022, 9, 23, 11, 23, 44)));
        savedEndpointHit4 = statRepository.save(EndpointHit.of(null, "app2", "/app2", "1.1.1.1",
                LocalDateTime.of(2022, 9, 25, 11, 23, 44)));
    }

    @Test
    @DisplayName("Find all hits")
    void find_withoutUri_shouldReturnAllHits() {
        List<ViewStats> stats = statRepository.findStats(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44));
        assertThat(stats.size(), is(2));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit2.getUri()));
        assertThat(stats.get(0).getHits(), is(3L));
        assertThat(stats.get(1).getUri(), is(savedEndpointHit1.getUri()));
        assertThat(stats.get(1).getHits(), is(1L));
    }

    @Test
    @DisplayName("Find all hits")
    void find_withBothUris_shouldReturnAllHits() {
        List<ViewStats> stats = statRepository.findStatsFromUrlList(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44), List.of("/app1", "/app2"));
        assertThat(stats.size(), is(2));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit2.getUri()));
        assertThat(stats.get(0).getHits(), is(3L));
        assertThat(stats.get(1).getUri(), is(savedEndpointHit1.getUri()));
        assertThat(stats.get(1).getHits(), is(1L));
    }

    @Test
    @DisplayName("Search by uri /app1 should return hit1")
    void find_withHit1Uri_shouldReturnStatWith1Hit() {
        List<ViewStats> stats = statRepository.findStatsFromUrlList(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44), List.of("/app1"));
        assertThat(stats.size(), is(1));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit1.getUri()));
        assertThat(stats.get(0).getHits(), is(1L));
    }

    @Test
    @DisplayName("Search by uri /app2 should return stat with 3 hits")
    void find_withHit2Uri_shouldReturnStatWith3Hits() {
        List<ViewStats> stats = statRepository.findStatsFromUrlList(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44), List.of("/app2"));
        assertThat(stats.size(), is(1));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit2.getUri()));
        assertThat(stats.get(0).getHits(), is(3L));
    }

    @Test
    @DisplayName("Search by uri /app2 and date should stat with 1 hit")
    void find_withHit2UriAndDate_shouldReturnStatWith1Hit() {
        List<ViewStats> stats = statRepository.findStatsFromUrlList(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 9, 24, 0, 0, 0), List.of("/app2"));
        assertThat(stats.size(), is(1));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit3.getUri()));
        assertThat(stats.get(0).getHits(), is(1L));
    }

    @Test
    @DisplayName("Search by uri /app2 and unique ip should return stat with 2 hits")
    void find_withHit2UriAndUniqueIp_shouldReturnStatWith2Hits() {
        List<ViewStats> stats = statRepository.findStatsFromListWithUniqueIps(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44), List.of("/app2"));
        assertThat(stats.size(), is(1));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit2.getUri()));
        assertThat(stats.get(0).getHits(), is(2L));
    }

    @Test
    @DisplayName("Search by all uris and unique ip should return 2 stats")
    void findAll_withdUniqueIp_shouldReturnStatWith2Stats() {
        List<ViewStats> stats = statRepository.findStatsWithUniqueIps(LocalDateTime.of(2021, 9, 21, 11, 23, 44),
                LocalDateTime.of(2022, 12, 21, 11, 23, 44));
        assertThat(stats.size(), is(2));
        assertThat(stats.get(0).getUri(), is(savedEndpointHit2.getUri()));
        assertThat(stats.get(0).getHits(), is(2L));
        assertThat(stats.get(1).getUri(), is(savedEndpointHit1.getUri()));
        assertThat(stats.get(1).getHits(), is(1L));
    }
}